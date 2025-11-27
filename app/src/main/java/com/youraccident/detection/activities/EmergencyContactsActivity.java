package com.youraccident.detection.activities;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youraccident.detection.R;
import com.youraccident.detection.adapters.ContactAdapter;
import com.youraccident.detection.models.EmergencyContact;
import com.youraccident.detection.utils.SharedPrefManager;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<EmergencyContact> contactList;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        sharedPrefManager = new SharedPrefManager(this);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        FloatingActionButton fabAddContact = findViewById(R.id.buttonAddContact);

        loadContacts();

        contactAdapter = new ContactAdapter(this, contactList, this::saveContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setAdapter(contactAdapter);

        fabAddContact.setOnClickListener(view -> {
            showAddContactDialog();
        });
    }

    private void loadContacts() {
        String contactsJson = sharedPrefManager.getEmergencyContacts();
        if (contactsJson != null && !contactsJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<EmergencyContact>>() {}.getType();
            contactList = gson.fromJson(contactsJson, type);
        } else {
            contactList = new ArrayList<>();
        }
    }

    private void saveContacts() {
        Gson gson = new Gson();
        String contactsJson = gson.toJson(contactList);
        sharedPrefManager.saveEmergencyContacts(contactsJson);
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText inputName = new EditText(this);
        inputName.setHint("Contact Name");
        layout.addView(inputName);

        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Phone Number");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            contactList.add(new EmergencyContact(name, phone, false));
            contactAdapter.notifyDataSetChanged();
            saveContacts();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
