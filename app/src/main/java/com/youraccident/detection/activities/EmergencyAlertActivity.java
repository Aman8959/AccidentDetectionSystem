package com.youraccident.detection.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youraccident.detection.R;
import com.youraccident.detection.models.Accident;
import com.youraccident.detection.models.EmergencyContact;
import com.youraccident.detection.utils.PhoneUtils;
import com.youraccident.detection.utils.SMSUtils;
import com.youraccident.detection.utils.SharedPrefManager;
import java.lang.reflect.Type;
import java.util.List;

public class EmergencyAlertActivity extends AppCompatActivity {

    private TextView textCountdown;
    private Button buttonCancel;
    private Button buttonCall;
    private CountDownTimer countDownTimer;
    private Accident accident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        textCountdown = findViewById(R.id.textCountdown);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonCall = findViewById(R.id.buttonCall);

        accident = (Accident) getIntent().getSerializableExtra("crash_data");

        buttonCancel.setOnClickListener(v -> {
            countDownTimer.cancel();
            finish();
        });

        buttonCall.setOnClickListener(v -> initiateEmergencyCall());

        startCountdown();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textCountdown.setText("Sending alert in: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                sendEmergencySms();
                initiateEmergencyCall();
                finish();
            }
        }.start();
    }

    private void sendEmergencySms() {
        String message = "Accident detected! Need help immediately.";
        if (accident != null) {
            message += " Location: http://maps.google.com/maps?q=" + accident.getLatitude() + "," + accident.getLongitude();
        }
        SMSUtils.sendEmergencySMS(this, message);
    }

    private void initiateEmergencyCall() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        String contactsJson = sharedPrefManager.getEmergencyContacts();
        if (contactsJson.isEmpty()) {
            return;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<EmergencyContact>>() {}.getType();
        List<EmergencyContact> contacts = gson.fromJson(contactsJson, type);

        if (contacts != null) {
            for (EmergencyContact contact : contacts) {
                if (contact.shouldCall()) {
                    PhoneUtils.makePhoneCall(this, contact.getPhoneNumber());
                    break; // Call only the first contact marked for calling
                }
            }
        }
    }
}
