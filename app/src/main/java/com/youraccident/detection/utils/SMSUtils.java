package com.youraccident.detection.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.youraccident.detection.models.User;

public class SMSUtils {

    public static boolean sendEmergencySMS(Context context, String message) {
        // Check SMS permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            boolean smsSent = false;

            // Get emergency contacts from SharedPrefManager
            SharedPrefManager prefManager = SharedPrefManager.getInstance(context);
            String[] emergencyContacts = prefManager.getEmergencyContacts();

            // Send to emergency contacts from SharedPreferences
            for (String contact : emergencyContacts) {
                if (contact != null && !contact.trim().isEmpty()) {
                    smsManager.sendTextMessage(contact.trim(), null, message, null, null);
                    smsSent = true;
                    System.out.println("SMS sent to: " + contact);
                }
            }

            // If no emergency contacts found in SharedPreferences, try User object
            if (!smsSent) {
                User user = prefManager.getUser();
                if (user != null) {
                    // Use the emergency contact methods from User class
                    String contact1 = user.getEmergencyContact1();
                    String contact2 = user.getEmergencyContact2();

                    if (contact1 != null && !contact1.isEmpty()) {
                        smsManager.sendTextMessage(contact1, null, message, null, null);
                        smsSent = true;
                        System.out.println("SMS sent to emergency contact 1: " + contact1);
                    }

                    if (contact2 != null && !contact2.isEmpty()) {
                        smsManager.sendTextMessage(contact2, null, message, null, null);
                        smsSent = true;
                        System.out.println("SMS sent to emergency contact 2: " + contact2);
                    }
                }
            }

            // If still no contacts, send to default numbers (for demo)
            if (!smsSent) {
                String[] defaultContacts = {"+911234567890", "+919876543210"}; // Demo numbers
                for (String contact : defaultContacts) {
                    try {
                        smsManager.sendTextMessage(contact, null, message, null, null);
                        smsSent = true;
                        System.out.println("SMS sent to default contact: " + contact);
                    } catch (Exception e) {
                        System.out.println("Failed to send to default contact: " + contact);
                    }
                }
            }

            if (smsSent) {
                Toast.makeText(context, "Emergency SMS sent to contacts", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_LONG).show();
            }

            return smsSent;

        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    // Additional method to send SMS to specific number
    public static boolean sendSMSToContact(Context context, String phoneNumber, String message) {
        // Check SMS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }
}