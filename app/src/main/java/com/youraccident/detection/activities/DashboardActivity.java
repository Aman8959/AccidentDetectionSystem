package com.youraccident.detection.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.youraccident.detection.R;
import com.youraccident.detection.models.User;
import com.youraccident.detection.utils.SharedPrefManager;

public class DashboardActivity extends AppCompatActivity {

    private Button buttonTracking, buttonEmergencyContacts, buttonLogout;
    private TextView textViewStatus, textViewUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        updateUserInfo();
        setClickListeners();
    }

    private void initViews() {
        // Buttons
        buttonTracking = findViewById(R.id.buttonTracking);
        buttonEmergencyContacts = findViewById(R.id.buttonEmergencyContacts);
        buttonLogout = findViewById(R.id.buttonLogout);

        // TextViews
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
    }

    private void updateUserInfo() {
        User user = SharedPrefManager.getInstance(this).getUser();
        if (user != null) {
            String userInfo = "User: " + user.getUsername() + "\nEmail: " + user.getEmail();
            textViewUserInfo.setText(userInfo);
        } else {
            textViewUserInfo.setText("User: Not logged in");
        }
    }

    private void setClickListeners() {
        // Start Tracking
        buttonTracking.setOnClickListener(v -> {
            startActivity(new Intent(this, TrackingActivity.class));
        });

        // Emergency Contacts
        buttonEmergencyContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        });

        // Logout
        buttonLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update status when returning to dashboard
        textViewStatus.setText("Accident Detection: Ready");
        updateUserInfo();
    }
}