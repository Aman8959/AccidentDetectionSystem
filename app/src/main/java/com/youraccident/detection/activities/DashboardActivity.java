package com.youraccident.detection.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.youraccident.detection.R;
import com.youraccident.detection.models.User;
import com.youraccident.detection.services.AccidentService;
import com.youraccident.detection.utils.SharedPrefManager;

public class DashboardActivity extends AppCompatActivity {

    private Button buttonTracking, buttonEmergencyContacts, buttonLogout;
    private TextView textViewStatus, textViewUserInfo;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize SharedPrefManager
        sharedPrefManager = new SharedPrefManager(this);

        // Start the background accident detection service
        startService(new Intent(this, AccidentService.class));

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
        
        // Update tracking button text to reflect its purpose
        buttonTracking.setText("Tracking Status");
    }

    private void updateUserInfo() {
        User user = sharedPrefManager.getUser();

        TextView welcome = findViewById(R.id.textWelcome);

        if (user != null) {
            welcome.setText("Welcome, " + user.getUsername());
            textViewUserInfo.setText("User: " + user.getEmail());
        } else {
            welcome.setText("Welcome, Guest");
            textViewUserInfo.setText("User: Not logged in");
        }
    }

    private void setClickListeners() {
        // Show tracking status on click
        buttonTracking.setOnClickListener(v -> {
            Toast.makeText(this, "Accident detection service is active.", Toast.LENGTH_SHORT).show();
        });

        // Open Emergency Contacts screen
        buttonEmergencyContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        });

        // Logout
        buttonLogout.setOnClickListener(v -> {
            sharedPrefManager.logout();
            stopService(new Intent(DashboardActivity.this, AccidentService.class)); // Stop the service on logout
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity(); // Finish all activities in the stack
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
