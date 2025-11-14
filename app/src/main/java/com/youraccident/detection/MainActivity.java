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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Check if user is already logged in
        checkLoginStatus();

        // Set click listeners
        setClickListeners();
    }



    private void checkLoginStatus() {
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    private void setClickListeners() {
        // Login button click
        buttonQuickLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        // Signup button click
        buttonQuickSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI based on login status
        updateUI();
    }

    private void updateUI() {
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            // User is logged in - hide buttons
            if (buttonQuickLogin != null) {
                buttonQuickLogin.setVisibility(View.GONE);
            }
            if (buttonQuickSignup != null) {
                buttonQuickSignup.setVisibility(View.GONE);
            }
        } else {
            // User is not logged in - show buttons
            if (buttonQuickLogin != null) {
                buttonQuickLogin.setVisibility(View.VISIBLE);
            }
            if (buttonQuickSignup != null) {
                buttonQuickSignup.setVisibility(View.VISIBLE);
            }
        }
    }
}