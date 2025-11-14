package com.youraccident.detection.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.youraccident.detection.R;
import com.youraccident.detection.models.User;
import com.youraccident.detection.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if user is already logged in
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return; // Add return to prevent further execution
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
    }

    private void setClickListeners() {
        buttonLogin.setOnClickListener(v -> loginUser());

        textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // For demo - In real app, verify with server
        if (username.equals("demo") && password.equals("demo123")) {
            User user = new User("1", username, "demo@example.com", "1234567890");
            SharedPrefManager.getInstance(this).saveUser(user);

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}