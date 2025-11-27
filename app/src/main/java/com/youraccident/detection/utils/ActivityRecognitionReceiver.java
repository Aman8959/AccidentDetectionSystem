package com.youraccident.detection.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.youraccident.detection.services.AccidentService;

public class ActivityRecognitionReceiver extends BroadcastReceiver {

    private static final String TAG = "ActivityRecognition";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result != null) {
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    handleTransitionEvent(context, event);
                }
            }
        }
    }

    private void handleTransitionEvent(Context context, ActivityTransitionEvent event) {
        if (event.getActivityType() == DetectedActivity.IN_VEHICLE) {
            Intent serviceIntent = new Intent(context, AccidentService.class);

            if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                Log.d(TAG, "User ENTERED a vehicle. Sending command to start listening.");
                serviceIntent.setAction(AccidentService.ACTION_START_LISTENING);
                context.startService(serviceIntent);

            } else if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                Log.d(TAG, "User EXITED a vehicle. Sending command to stop listening.");
                serviceIntent.setAction(AccidentService.ACTION_STOP_LISTENING);
                context.startService(serviceIntent);
            }
        }
    }
}
