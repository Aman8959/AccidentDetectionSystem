package com.youraccident.detection.activities;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youraccident.detection.R;
import com.youraccident.detection.adapters.EmergencyContactsAdapter;
import com.youraccident.detection.models.EmergencyContact;
import com.youraccident.detection.utils.SharedPrefManager;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity implements EmergencyContactsAdapter.OnDeleteButtonClickListener {

    private ListView listViewContacts;
    private List<EmergencyContact> contacts;
    private EmergencyContactsAdapter adapter;
    private SharedPrefManager sharedPrefManager;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        sharedPrefManager = new SharedPrefManager(this);
        gson = new Gson();
        listViewContacts = findViewById(R.id.listViewContacts);

        loadContacts();

        adapter = new EmergencyContactsAdapter(this, contacts, this);
        listViewContacts.setAdapter(adapter);

        findViewById(R.id.buttonAddContact).setOnClickListener(v -> showAddContactDialog());
    }

    private void loadContacts() {
        String contactsJson = sharedPrefManager.getEmergencyContacts();
        if (contactsJson.isEmpty()) {
            contacts = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<EmergencyContact>>() {}.getType();
            contacts = gson.fromJson(contactsJson, type);
        }
    }

    private void saveContacts() {
        String contactsJson = gson.toJson(contacts);
        sharedPrefManager.saveEmergencyContacts(contactsJson);
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextNumber = dialogView.findViewById(R.id.editTextNumber);
        CheckBox checkBoxShouldCall = dialogView.findViewById(R.id.checkboxShouldCall);

        builder.setTitle("Add Emergency Contact")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editTextName.getText().toString().trim();
                        String number = editTextNumber.getText().toString().trim();
                        boolean shouldCall = checkBoxShouldCall.isChecked();
                        if (!name.isEmpty() && !number.isEmpty()) {
                            contacts.add(new EmergencyContact(name, number, shouldCall));
                            adapter.notifyDataSetChanged();
                            saveContacts();
                        } else {
                            Toast.makeText(EmergencyContactsActivity.this, "Please enter both name and number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    @Override
    public void onDeleteClick(int position) {
        contacts.remove(position);
        adapter.notifyDataSetChanged();
        saveContacts();
    }
}
