package com.youraccident.detection.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PhoneUtils {

    private static final String TAG = "PhoneUtils";

    public static void makePhoneCall(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.e(TAG, "Invalid phone number provided.");
            return;
        }
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
            Log.d(TAG, "Initiating phone call to: " + phoneNumber);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to make phone call due to security exception. Check CALL_PHONE permission.", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to make phone call to: " + phoneNumber, e);
        }
    }
}
