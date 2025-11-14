package com.youraccident.detection.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Sensor service started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate G-force
            double gForce = Math.sqrt(x * x + y * y + z * z);

            // You can add accident detection logic here
            // and communicate with TrackingActivity
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for basic implementation
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        Toast.makeText(this, "Sensor service stopped", Toast.LENGTH_SHORT).show();
    }
}