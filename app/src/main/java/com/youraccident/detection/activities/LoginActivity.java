package com.youraccident.detection.activities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private SharedPrefManager sharedPrefManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPrefManager first
        sharedPrefManager = new SharedPrefManager(this);

        // Check if user is already logged in
        if (sharedPrefManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return; // Stop further execution
        }

        setContentView(R.layout.activity_login);

        initViews();
        setClickListeners();
        
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail); // Ensure ID matches layout
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
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Login
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Fetch user details from Firestore to get username, etc.
                        // This part is important for a complete user profile.
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null) {
                                        sharedPrefManager.saveUser(user);
                                    }
                                    // Even if user not in DB, login should proceed
                                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch user data, but still log in
                                    Toast.makeText(LoginActivity.this, "Login Successful! (Could not fetch profile)", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                    finish();
                                });

                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Invalid Credentials or User not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
