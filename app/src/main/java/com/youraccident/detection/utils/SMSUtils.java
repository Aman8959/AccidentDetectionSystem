package com.youraccident.detection.utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youraccident.detection.models.EmergencyContact;
import java.lang.reflect.Type;
import java.util.List;

public class SMSUtils {

    private static final String TAG = "SMSUtils";

    public static void sendSMS(Context context, String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.e(TAG, "Invalid phone number provided.");
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS sent successfully to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS to: " + phoneNumber, e);
        }
    }

    public static boolean sendEmergencySMS(Context context, String message) {
        try {
            SharedPrefManager prefManager = new SharedPrefManager(context);
            String contactsJson = prefManager.getEmergencyContacts();
            if (contactsJson.isEmpty()) {
                Log.d(TAG, "No emergency contacts found in SharedPreferences.");
                return false;
            }

            Gson gson = new Gson();
            Type type = new TypeToken<List<EmergencyContact>>() {}.getType();
            List<EmergencyContact> contacts = gson.fromJson(contactsJson, type);

            if (contacts == null || contacts.isEmpty()) {
                Log.d(TAG, "No emergency contacts found after parsing JSON.");
                return false;
            }

            for (EmergencyContact contact : contacts) {
                sendSMS(context, contact.getPhoneNumber(), message);
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "An error occurred while sending emergency SMS", e);
            return false;
        }
    }
}
