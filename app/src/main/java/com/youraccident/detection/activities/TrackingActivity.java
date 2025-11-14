package com.youraccident.detection.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.youraccident.detection.R;
import com.youraccident.detection.utils.SMSUtils;

public class TrackingActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewStatus, textViewCountdown;
    private Button buttonStopTracking;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isTracking = false;
    private boolean accidentDetected = false;
    private CountDownTimer countDownTimer;
    private static final float ACCIDENT_THRESHOLD = 15.0f; // G-force threshold
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initViews();
        initSensors();
        checkPermissionsAndStartTracking();
    }

    private void initViews() {
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewCountdown = findViewById(R.id.textViewCountdown);
        buttonStopTracking = findViewById(R.id.buttonStopTracking);

        buttonStopTracking.setOnClickListener(v -> {
            if (accidentDetected) {
                cancelAccidentAlert();
            } else {
                stopTracking();
            }
        });
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void checkPermissionsAndStartTracking() {
        if (hasRequiredPermissions()) {
            startTracking();
        } else {
            requestPermissions();
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                Toast.makeText(this, "Permissions denied! Tracking cannot start.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startTracking() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            isTracking = true;
            textViewStatus.setText("Status: Tracking Active");
            Toast.makeText(this, "Accident detection started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void stopTracking() {
        if (isTracking) {
            sensorManager.unregisterListener(this);
            isTracking = false;
            textViewStatus.setText("Status: Tracking Stopped");
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cancelAccidentAlert() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        accidentDetected = false;
        textViewStatus.setText("Status: Alert Cancelled");
        textViewCountdown.setText("");
        buttonStopTracking.setText("STOP TRACKING");
        Toast.makeText(this, "Accident alert cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !accidentDetected) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate G-force (remove gravity)
            double gForce = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            // Check if accident detected
            if (gForce > ACCIDENT_THRESHOLD) {
                accidentDetected = true;
                handleAccidentDetection();
            }
        }
    }

    private void handleAccidentDetection() {
        textViewStatus.setText("Status: Accident Detected!");

        // Start 10-second countdown for cancellation
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textViewCountdown.setText("Cancel in: " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                // User didn't cancel - send emergency alerts
                sendEmergencyAlerts();
            }
        }.start();

        buttonStopTracking.setText("CANCEL ALERT");
    }

    private void sendEmergencyAlerts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        String message;
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            message = "EMERGENCY: Accident detected! Location: " +
                                    "https://maps.google.com/?q=" + latitude + "," + longitude;
                        } else {
                            message = "EMERGENCY: Accident detected! Unable to get location.";
                        }

                        // Send SMS alerts
                        boolean smsSent = SMSUtils.sendEmergencySMS(TrackingActivity.this, message);

                        if (smsSent) {
                            Toast.makeText(TrackingActivity.this,
                                    "Emergency alerts sent!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TrackingActivity.this,
                                    "Failed to send alerts!", Toast.LENGTH_LONG).show();
                        }

                        finish();
                    });
        } else {
            // Send SMS without location
            String message = "EMERGENCY: Accident detected! Location unavailable.";
            boolean smsSent = SMSUtils.sendEmergencySMS(this, message);

            if (smsSent) {
                Toast.makeText(this, "Emergency alerts sent!", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking && !accidentDetected) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTracking && !accidentDetected && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        sensorManager.unregisterListener(this);
    }
}