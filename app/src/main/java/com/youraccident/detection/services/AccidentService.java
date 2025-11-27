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
    private static final String SOS_NOTIFICATION_CHANNEL_ID = "SOSChannel";

    private SensorManager sensorManager;
    private ActivityRecognitionClient activityRecognitionClient;

    // Using original logic with very low threshold for final test
    private static final float PEAK_G_FORCE_THRESHOLD = 1.8f; 
    private static final float LOW_PASS_ALPHA = 0.8f;
    private float[] gravity = new float[3];

    private long lastCrashTriggerTime = 0;
    private static final long CRASH_COOLDOWN_PERIOD = 5000; // 5 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate: Initializing...");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        activityRecognitionClient = ActivityRecognition.getClient(this);
        startForegroundService();
        startSensorListening();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_START_LISTENING)) {
                startSensorListening();
            } else if (intent.getAction().equals(ACTION_STOP_LISTENING)) {
                stopSensorListening();
            }
        }
        return START_STICKY;
    }

    public void startSensorListening() {
        Log.d(TAG, "Started sensor listening.");
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stopSensorListening() {
        Log.d(TAG, "Stopped sensor listening.");
        sensorManager.unregisterListener(this);
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
        double gForce = magnitude / SensorManager.GRAVITY_EARTH;

        Log.d(TAG, "Current Linear G-Force: " + String.format("%.2f", gForce));

        if (gForce > PEAK_G_FORCE_THRESHOLD) {
            if (System.currentTimeMillis() - lastCrashTriggerTime > CRASH_COOLDOWN_PERIOD) {
                Log.w(TAG, "LINEAR G-FORCE THRESHOLD EXCEEDED! G-Force: " + gForce);
                lastCrashTriggerTime = System.currentTimeMillis();
                triggerSOSProtocol(gForce, new ArrayList<>());
            }
        }
    }

    private void triggerSOSProtocol(double peakGForce, List<Double> buffer) {
        GPSUtils.getCurrentLocation(this, location -> {
            Accident crashData = new Accident();
            crashData.setTimestamp(new Date());
            crashData.setImpactMagnitude(peakGForce);
            if (location != null) {
                crashData.setLatitude(location.getLatitude());
                crashData.setLongitude(location.getLongitude());
                crashData.setSpeedBeforeCrash(location.getSpeed() * 3.6f);
            } else {
                crashData.setLatitude(0.0);
                crashData.setLongitude(0.0);
                crashData.setSpeedBeforeCrash(0.0f);
            }

            crashData.setSeverity(calculateSeverity(peakGForce, 0));
            showSOSNotification(crashData);
        });
    }

    private void showSOSNotification(Accident crashData) {
        createSOSNotificationChannel();

        Intent fullScreenIntent = new Intent(this, SOSActivity.class);
        fullScreenIntent.putExtra("crash_data", crashData);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SOS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CRASH DETECTED!")
                .setContentText("Tap to open the SOS screen.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    private String calculateSeverity(double peakG, double deltaV) {
        if (peakG > 4) return "HIGH";
        if (peakG > 2.5) return "MEDIUM";
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
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Accident Detection System")
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

    private void createSOSNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    SOS_NOTIFICATION_CHANNEL_ID,
                    "SOS Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for displaying crash alerts.");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
