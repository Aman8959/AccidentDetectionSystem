package com.youraccident.detection.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.youraccident.detection.R;
import com.youraccident.detection.models.User;
import com.youraccident.detection.utils.SharedPrefManager;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText editTextContact1, editTextContact2;
    private Button buttonSaveContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        initViews();
        loadExistingContacts();
        setClickListeners();
    }

    private void initViews() {
        editTextContact1 = findViewById(R.id.editTextContact1);
        editTextContact2 = findViewById(R.id.editTextContact2);
        buttonSaveContacts = findViewById(R.id.buttonSaveContacts);
    }

    private void loadExistingContacts() {
        User user = SharedPrefManager.getInstance(this).getUser();
        if (user != null) {
            if (user.getEmergencyContact1() != null) {
                editTextContact1.setText(user.getEmergencyContact1());
            }
            if (user.getEmergencyContact2() != null) {
                editTextContact2.setText(user.getEmergencyContact2());
            }
        }
    }

    private void setClickListeners() {
        buttonSaveContacts.setOnClickListener(v -> saveEmergencyContacts());
    }

    private void saveEmergencyContacts() {
        String contact1 = editTextContact1.getText().toString().trim();
        String contact2 = editTextContact2.getText().toString().trim();

        if (contact1.isEmpty() && contact2.isEmpty()) {
            Toast.makeText(this, "Please add at least one emergency contact", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = SharedPrefManager.getInstance(this).getUser();
        if (user != null) {
            user.setEmergencyContact1(contact1);
            user.setEmergencyContact2(contact2);
            SharedPrefManager.getInstance(this).saveUser(user);

            Toast.makeText(this, "Emergency contacts saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }
}