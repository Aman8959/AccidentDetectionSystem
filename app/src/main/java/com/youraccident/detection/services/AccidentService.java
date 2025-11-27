package com.youraccident.detection.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.youraccident.detection.R;
import com.youraccident.detection.activities.SOSActivity;
import com.youraccident.detection.models.Accident;
import com.youraccident.detection.utils.ActivityRecognitionReceiver;
import com.youraccident.detection.utils.GPSUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccidentService extends Service implements SensorEventListener {

    private static final String TAG = "AccidentService";
    public static final String ACTION_START_LISTENING = "com.youraccident.detection.START_LISTENING";
    public static final String ACTION_STOP_LISTENING = "com.youraccident.detection.STOP_LISTENING";
    private static final String NOTIFICATION_CHANNEL_ID = "AccidentDetectionChannel";

    private SensorManager sensorManager;
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityTransitionPendingIntent;

    // TESTING: Lowered thresholds for easier testing.
    private static final int SENSOR_SAMPLING_RATE = SensorManager.SENSOR_DELAY_GAME;
    private static final double IMPACT_ENERGY_THRESHOLD = 40; // Original: 350
    private static final float PEAK_G_FORCE_THRESHOLD = 3.0f; // Original: 8.0f
    private static final float LOW_PASS_ALPHA = 0.8f;
    private float[] gravity = new float[3];
    private static final int DATA_BUFFER_SIZE = 25;
    private final List<Double> accelerationBuffer = new ArrayList<>();

    private long lastCrashTriggerTime = 0;
    private static final long CRASH_COOLDOWN_PERIOD = 20000; // 20 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate: Initializing...");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        activityRecognitionClient = ActivityRecognition.getClient(this);

        startForegroundService();
        // TESTING: Bypass activity recognition for easier testing.
        startSensorListening();
        // setupActivityTransitions(); // Original logic
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Original logic for starting/stopping via activity recognition is bypassed in onCreate for now.
        return START_STICKY;
    }

    private void setupActivityTransitions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "CRITICAL ERROR: ACTIVITY_RECOGNITION permission not granted. Service cannot detect driving and will not function.");
            stopSelf();
            return;
        }

        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Intent intent = new Intent(this, ActivityRecognitionReceiver.class);
        activityTransitionPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        activityRecognitionClient.requestActivityTransitionUpdates(request, activityTransitionPendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Activity transition updates requested successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to request activity transition updates.", e));
    }

    public void startSensorListening() {
        Log.d(TAG, "Started sensor listening (TEST MODE)." );
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SENSOR_SAMPLING_RATE);
        }
    }

    public void stopSensorListening() {
        Log.d(TAG, "Stopped sensor listening.");
        sensorManager.unregisterListener(this);
        accelerationBuffer.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometerEvent(event);
        }
    }

    private void processAccelerometerEvent(SensorEvent event) {
        final float alpha = LOW_PASS_ALPHA;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float linear_acceleration_x = event.values[0] - gravity[0];
        float linear_acceleration_y = event.values[1] - gravity[1];
        float linear_acceleration_z = event.values[2] - gravity[2];

        double magnitude = Math.sqrt(Math.pow(linear_acceleration_x, 2) + Math.pow(linear_acceleration_y, 2) + Math.pow(linear_acceleration_z, 2));
        magnitude = magnitude / SensorManager.GRAVITY_EARTH;

        accelerationBuffer.add(magnitude);
        if (accelerationBuffer.size() > DATA_BUFFER_SIZE) {
            accelerationBuffer.remove(0);
        }

        if (accelerationBuffer.size() == DATA_BUFFER_SIZE) {
            detectCrashSignature(new ArrayList<>(accelerationBuffer));
        }
    }

    private void detectCrashSignature(List<Double> buffer) {
        double peakGForce = 0;
        double totalEnergy = 0;

        for (Double g : buffer) {
            totalEnergy += g * g;
            if (g > peakGForce) {
                peakGForce = g;
            }
        }

        if (peakGForce > PEAK_G_FORCE_THRESHOLD && totalEnergy > IMPACT_ENERGY_THRESHOLD) {
            Log.w(TAG, "CRASH SIGNATURE DETECTED! (TEST) Peak G: " + peakGForce + ", Energy: " + totalEnergy);

            if (System.currentTimeMillis() - lastCrashTriggerTime > CRASH_COOLDOWN_PERIOD) {
                lastCrashTriggerTime = System.currentTimeMillis();
                triggerSOSProtocol(peakGForce, buffer);
            }
        }
    }

    private void triggerSOSProtocol(double peakGForce, List<Double> buffer) {
        GPSUtils.getCurrentLocation(this, location -> {
            if (location == null) {
                Log.e(TAG, "Cannot trigger SOS, location is not available.");
                // For testing, we can proceed without location
            }

            Accident crashData = new Accident();
            crashData.setTimestamp(new Date());
            crashData.setImpactMagnitude(peakGForce);
            if (location != null) {
                crashData.setLatitude(location.getLatitude());
                crashData.setLongitude(location.getLongitude());
                crashData.setSpeedBeforeCrash(location.getSpeed() * 3.6f);
            } else {
                // Mock data for testing when location is null
                crashData.setLatitude(0.0);
                crashData.setLongitude(0.0);
                crashData.setSpeedBeforeCrash(0.0f);
            }

            double deltaV = (peakGForce * 9.8 * 0.15) * 3.6;
            crashData.setDeltaV(deltaV);
            crashData.setSeverity(calculateSeverity(peakGForce, deltaV));

            List<Float> timeline = new ArrayList<>();
            for (Double g : buffer) {
                timeline.add(g.floatValue());
            }
            crashData.setPreCrashGForceTimeline(timeline);

            Intent alertIntent = new Intent(this, SOSActivity.class);
            alertIntent.putExtra("crash_data", crashData);
            alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(alertIntent);
        });
    }

    private String calculateSeverity(double peakG, double deltaV) {
        if (peakG > 40 || deltaV > 50) return "CRITICAL";
        if (peakG > 25 || deltaV > 35) return "HIGH";
        if (peakG > 15 || deltaV > 20) return "MEDIUM";
        return "LOW";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy: Cleaning up.");
        stopSensorListening();
        // ... (original cleanup logic) ...
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Accident Detection System (TEST MODE)")
                .setContentText("Monitoring for your safety in the background.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Background Safety Service",
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
