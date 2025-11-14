package com.youraccident.detection.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GPSUtils {
    private static final String TAG = "GPSUtils";

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