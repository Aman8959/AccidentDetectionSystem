package com.youraccident.detection.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youraccident.detection.R;
import com.youraccident.detection.models.Accident;
import com.youraccident.detection.models.EmergencyContact;
import com.youraccident.detection.utils.SMSUtils;
import com.youraccident.detection.utils.SharedPrefManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class SOSActivity extends AppCompatActivity {

    private static final String TAG = "SOSActivity";
    private static final long COUNTDOWN_TIME = 15000; // 15 seconds

    private TextView textViewCountdown;
    private Button buttonCancelSos;

    private CountDownTimer countDownTimer;
    private Accident crashData;
    private boolean alertSent = false;
    private MediaPlayer alertPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_sos);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            crashData = getIntent().getParcelableExtra("crash_data", Accident.class);
        } else {
            crashData = getIntent().getParcelableExtra("crash_data");
        }

        if (crashData == null) {
            Log.e(TAG, "SOSActivity started without crash data. Finishing.");
            finish();
            return;
        }

        initViews();
        setClickListeners();
        startCountdown();
        startAlertSound();
    }

    private void initViews() {
        textViewCountdown = findViewById(R.id.textViewCountdown);
        buttonCancelSos = findViewById(R.id.buttonCancelSos);
    }

    private void setClickListeners() {
        buttonCancelSos.setOnClickListener(v -> cancelAlert());
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                textViewCountdown.setText("0");
                if (!alertSent) {
                    sendAlerts();
                }
            }
        }.start();
    }

    private void startAlertSound() {
        try {
            alertPlayer = MediaPlayer.create(this, R.raw.warning);
            alertPlayer.setLooping(true);
            alertPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing alert sound", e);
        }
    }

    private void stopAlertSound() {
        if (alertPlayer != null && alertPlayer.isPlaying()) {
            alertPlayer.stop();
            alertPlayer.release();
            alertPlayer = null;
        }
    }

    private void cancelAlert() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAlertSound();
        Toast.makeText(this, "Alert Canceled", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "SOS alert was canceled by the user.");
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendAlerts() {
        alertSent = true;
        stopAlertSound();
        Log.i(TAG, "Countdown finished. Sending emergency alerts NOW.");

        SharedPrefManager prefManager = new SharedPrefManager(this);
        String contactsJson = prefManager.getEmergencyContacts();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            crashData.setUserId(userId);
        }
        crashData.setAccidentId(UUID.randomUUID().toString());

        if (contactsJson != null && !contactsJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<EmergencyContact>>() {}.getType();
            List<EmergencyContact> contacts = gson.fromJson(contactsJson, type);

            if (contacts != null && !contacts.isEmpty()) {
                String message = String.format(
                        "CRASH DETECTED! Severity: %s. Location: http://maps.google.com/maps?q=%.6f,%.6f",
                        crashData.getSeverity(),
                        crashData.getLatitude(),
                        crashData.getLongitude()
                );
                for (EmergencyContact contact : contacts) {
                    SMSUtils.sendSMS(this, contact.getPhoneNumber(), message);
                }

                // Make a call to the primary contact
                EmergencyContact primaryContact = contacts.get(0);
                makePhoneCall(primaryContact.getPhoneNumber());

            } else {
                Log.w(TAG, "Cannot send SMS. Contact list is empty after parsing.");
            }
        } else {
            Log.w(TAG, "Cannot send SMS. No emergency contacts set.");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("crash_reports").document(crashData.getAccidentId())
                .set(crashData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Crash report saved to Firestore."))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving crash report to Firestore", e));

        Toast.makeText(this, "Emergency Alert Sent!", Toast.LENGTH_LONG).show();
        new android.os.Handler(Looper.getMainLooper()).postDelayed(this::finish, 5000);
    }

    private void makePhoneCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            // TODO: Request permission
            Log.e(TAG, "CALL_PHONE permission not granted.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAlertSound();
    }
}
