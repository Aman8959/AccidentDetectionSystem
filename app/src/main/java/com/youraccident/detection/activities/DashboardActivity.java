package com.youraccident.detection.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.youraccident.detection.R;
import com.youraccident.detection.models.Accident;
import com.youraccident.detection.models.User;
import com.youraccident.detection.services.AccidentService;
import com.youraccident.detection.utils.SharedPrefManager;

import java.util.Date;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSIONS_REQUEST_CODE = 101;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    private Button buttonTracking, buttonEmergencyContacts, buttonLogout;
    private TextView textWelcome, textViewStatus, textViewStatusReady, textViewUserInfo, textViewUserStatus;
    private ImageView imageViewUserIcon, status_dot_ready, status_dot_login;
    private SharedPrefManager sharedPrefManager;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long mShakeTimestamp;
    private int mShakeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sharedPrefManager = new SharedPrefManager(this);

        if (sharedPrefManager.isLoggedIn()) {
            startService(new Intent(this, AccidentService.class));
        }

        initViews();
        setClickListeners();
        checkAndRequestPermissions();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initViews() {
        // Buttons
        buttonTracking = findViewById(R.id.buttonTracking);
        buttonEmergencyContacts = findViewById(R.id.buttonEmergencyContacts);
        buttonLogout = findViewById(R.id.buttonLogout);

        // TextViews
        textWelcome = findViewById(R.id.textWelcome);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatusReady = findViewById(R.id.textViewStatusReady);
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        textViewUserStatus = findViewById(R.id.textViewUserStatus);

        // ImageViews
        imageViewUserIcon = findViewById(R.id.imageViewUserIcon);
        status_dot_ready = findViewById(R.id.status_dot_ready);
        status_dot_login = findViewById(R.id.status_dot_login);
    }

    private void updateUserInfo() {
        User user = sharedPrefManager.getUser();

        if (user != null) {
            textWelcome.setText("Welcome, " + user.getUsername());
            textViewUserStatus.setText("Logged In");
            status_dot_login.setImageResource(R.drawable.status_dot_green);
            textViewStatusReady.setText("Ready");
            status_dot_ready.setImageResource(R.drawable.status_dot_green);
            imageViewUserIcon.setImageResource(R.drawable.ic_person_placeholder);

        } else {
            textWelcome.setText("Welcome, Guest");
            textViewUserStatus.setText("Not logged in");
            status_dot_login.setImageResource(R.drawable.status_dot_red);
            textViewStatusReady.setText("Not Active");
            status_dot_ready.setImageResource(R.drawable.status_dot_red);
            imageViewUserIcon.setImageResource(R.drawable.ic_person_placeholder);
        }
    }

    private void setClickListeners() {
        buttonTracking.setOnClickListener(v -> {
            if (sharedPrefManager.isLoggedIn()) {
                Toast.makeText(this, "Accident detection service is active.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please log in to use this feature.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonEmergencyContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        });

        buttonLogout.setOnClickListener(v -> {
            sharedPrefManager.logout();
            stopService(new Intent(DashboardActivity.this, AccidentService.class));
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions were denied. The app might not function correctly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserInfo();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                triggerAlert();
            }
        }
    }

    private void triggerAlert() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Accident testCrashData = new Accident();
        testCrashData.setTimestamp(new Date());
        testCrashData.setImpactMagnitude(5.0); // Example value
        testCrashData.setLatitude(0.0);
        testCrashData.setLongitude(0.0);
        testCrashData.setSeverity("TEST");

        Intent intent = new Intent(this, SOSActivity.class);
        intent.putExtra("crash_data", testCrashData);
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not used
    }
}
