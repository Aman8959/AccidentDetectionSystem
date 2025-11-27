package com.youraccident.detection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.youraccident.detection.activities.DashboardActivity;
import com.youraccident.detection.activities.LoginActivity;
import com.youraccident.detection.activities.SignUpActivity;
import com.youraccident.detection.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private Button buttonQuickLogin, buttonQuickSignup;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPrefManager first
        sharedPrefManager = new SharedPrefManager(this);

        // Check if user is already logged in and redirect
        if (sharedPrefManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return; // Important to prevent rest of the code from executing
        }

        setContentView(R.layout.activity_main);

        // Initialize views after setting content view
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        buttonQuickLogin = findViewById(R.id.buttonQuickLogin);
        buttonQuickSignup = findViewById(R.id.buttonQuickSignup);
    }

    private void setClickListeners() {
        // Login button click
        buttonQuickLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        // Signup button click
        buttonQuickSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });
    }

    // No need for onResume and updateUI as we are redirecting at the start
    // if the user is already logged in.
}
