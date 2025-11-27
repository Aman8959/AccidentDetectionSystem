package com.youraccident.detection.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.function.Consumer;

public class GPSUtils {
    private static final String TAG = "GPSUtils";

    // Callback interface for location updates
    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    public static void getCurrentLocation(Context context, Consumer<Location> callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // 1. Check for permissions (this is crucial!)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted.");
            // In a real app, you should request permissions here.
            callback.accept(null);
            return;
        }

        // 2. Try to get the current location
        // This provides a fresh location and is better for one-time requests.
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Current location received: " + location.getLatitude() + ", " + location.getLongitude());
                        callback.accept(location);
                    } else {
                        // If current location is null, try last known location as a fallback
                        Log.w(TAG, "Current location is null, trying last known location.");
                        getLastKnownLocation(fusedLocationClient, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get current location", e);
                    getLastKnownLocation(fusedLocationClient, callback); // Fallback on failure
                });
    }

    private static void getLastKnownLocation(FusedLocationProviderClient client, Consumer<Location> callback) {
        try {
            client.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Last known location received: " + location.getLatitude() + ", " + location.getLongitude());
                        }
                        callback.accept(location); // Can be null
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last known location", e);
                        callback.accept(null);
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "Location permission not granted for getLastLocation.", se);
            callback.accept(null);
        }
    }


    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String formatLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            return "Latitude: " + lat + ", Longitude: " + lon;
        }
        return "Location not available";
    }

    public static String getGoogleMapsLink(double latitude, double longitude) {
        return "https://maps.google.com/?q=" + latitude + "," + longitude;
    }
}
