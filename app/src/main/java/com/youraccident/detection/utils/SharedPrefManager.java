package com.youraccident.detection.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.youraccident.detection.models.User;

public class SharedPrefManager {
    private static final String PREF_NAME = "AccidentDetectionPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";
    private static final String KEY_EMERGENCY_CONTACT_1 = "emergency_contact_1";
    private static final String KEY_EMERGENCY_CONTACT_2 = "emergency_contact_2";

    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER, userJson);

        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUser() {
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Emergency Contacts Management
    public void saveEmergencyContact1(String phoneNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMERGENCY_CONTACT_1, phoneNumber);
        editor.apply();
    }

    public void saveEmergencyContact2(String phoneNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMERGENCY_CONTACT_2, phoneNumber);
        editor.apply();
    }

    public String getEmergencyContact1() {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACT_1, "");
    }

    public String getEmergencyContact2() {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACT_2, "");
    }

    public void saveEmergencyContacts(String contact1, String contact2) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMERGENCY_CONTACT_1, contact1);
        editor.putString(KEY_EMERGENCY_CONTACT_2, contact2);
        editor.apply();

        // Also update the user object if exists
        User user = getUser();
        if (user != null) {
            user.setEmergencyContact1(contact1);
            user.setEmergencyContact2(contact2);
            saveUser(user);
        }
    }

    public String[] getEmergencyContacts() {
        String contact1 = sharedPreferences.getString(KEY_EMERGENCY_CONTACT_1, "");
        String contact2 = sharedPreferences.getString(KEY_EMERGENCY_CONTACT_2, "");
        return new String[]{contact1, contact2};
    }

    public boolean hasEmergencyContacts() {
        String contact1 = getEmergencyContact1();
        String contact2 = getEmergencyContact2();
        return (contact1 != null && !contact1.isEmpty()) ||
                (contact2 != null && !contact2.isEmpty());
    }

    // Additional utility methods
    public void setFirstTimeLaunch(boolean isFirstTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_first_time", isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean("is_first_time", true);
    }

    public void setTrackingEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("tracking_enabled", enabled);
        editor.apply();
    }

    public boolean isTrackingEnabled() {
        return sharedPreferences.getBoolean("tracking_enabled", true);
    }

    public void saveLastKnownLocation(String latitude, String longitude) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_latitude", latitude);
        editor.putString("last_longitude", longitude);
        editor.putLong("last_location_timestamp", System.currentTimeMillis());
        editor.apply();
    }

    public String getLastKnownLatitude() {
        return sharedPreferences.getString("last_latitude", "0.0");
    }

    public String getLastKnownLongitude() {
        return sharedPreferences.getString("last_longitude", "0.0");
    }

    public long getLastLocationTimestamp() {
        return sharedPreferences.getLong("last_location_timestamp", 0);
    }

    // For accident detection sensitivity
    public void setAccidentSensitivity(float sensitivity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("accident_sensitivity", sensitivity);
        editor.apply();
    }

    public float getAccidentSensitivity() {
        return sharedPreferences.getFloat("accident_sensitivity", 15.0f); // Default threshold
    }

    // For notification preferences
    public void setNotificationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", enabled);
        editor.apply();
    }

    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean("notifications_enabled", true);
    }
}