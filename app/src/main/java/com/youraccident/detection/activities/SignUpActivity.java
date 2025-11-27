package com.youraccident.detection.activities;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.youraccident.detection.R;
import com.youraccident.detection.models.EmergencyContact;
import com.youraccident.detection.models.User;
import com.youraccident.detection.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPhone, editTextPassword, editTextConfirmPassword, editTextEmergencyContact;
    private Button buttonSignUp;
    private TextView textViewLogin;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setClickListeners();
        auth = FirebaseAuth.getInstance();

    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextEmergencyContact = findViewById(R.id.editTextEmergencyContact);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void setClickListeners() {
        buttonSignUp.setOnClickListener(v -> registerUser());

        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String emergencyContact = editTextEmergencyContact.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || emergencyContact.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10 || emergencyContact.length() < 10) {
            Toast.makeText(this, "Please enter valid phone numbers", Toast.LENGTH_SHORT).show();
            return;
        }


        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();

                        User user = new User(
                                uid,
                                username,
                                email,
                                phone
                        );


                        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
                        sharedPrefManager.saveUser(user);

                        List<EmergencyContact> contacts = new ArrayList<>();

                        contacts.add(new EmergencyContact("Primary Contact", emergencyContact, false));

                        String contactsJson = new Gson().toJson(contacts);

                        sharedPrefManager.saveEmergencyContacts(contactsJson);

                        Toast.makeText(SignUpActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class); 
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(SignUpActivity.this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
