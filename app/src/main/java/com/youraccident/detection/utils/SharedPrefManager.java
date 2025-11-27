package com.youraccident.detection.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.youraccident.detection.models.User;

public class SharedPrefManager {
    private static final String PREF_NAME = "AccidentDetectionPref";
    private static final String KEY_USER = "user";
    private static final String KEY_EMERGENCY_CONTACTS = "emergency_contacts";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER, userJson).apply();
    }

    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getUser() != null;
    }

    public void logout() {
        sharedPreferences.edit().remove(KEY_USER).remove(KEY_EMERGENCY_CONTACTS).apply();
    }

    public void saveEmergencyContacts(String contactsJson) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_CONTACTS, contactsJson).apply();
    }

    public String getEmergencyContacts() {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACTS, "");
    }

    public boolean hasEmergencyContacts() {
        String contacts = getEmergencyContacts();
        return contacts != null && !contacts.trim().isEmpty();
    }
}
