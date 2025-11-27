package com.youraccident.detection.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Accident implements Parcelable {

    // Core Details
    private String accidentId;
    private String userId;
    private Date timestamp;
    private double latitude;
    private double longitude;

    // Calculated Crash Parameters
    private double impactMagnitude;
    private double deltaV;
    private float speedBeforeCrash;
    private String severity;

    // Contextual & Forensic Data
    private String vehicleType; // (e.g., "car", "motorcycle")
    private List<Float> preCrashGForceTimeline;

    // Default constructor for Firebase
    public Accident() {}

    // Getters and Setters
    public String getAccidentId() { return accidentId; }
    public void setAccidentId(String accidentId) { this.accidentId = accidentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getImpactMagnitude() { return impactMagnitude; }
    public void setImpactMagnitude(double impactMagnitude) { this.impactMagnitude = impactMagnitude; }

    public double getDeltaV() { return deltaV; }
    public void setDeltaV(double deltaV) { this.deltaV = deltaV; }

    public float getSpeedBeforeCrash() { return speedBeforeCrash; }
    public void setSpeedBeforeCrash(float speedBeforeCrash) { this.speedBeforeCrash = speedBeforeCrash; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public List<Float> getPreCrashGForceTimeline() { return preCrashGForceTimeline; }
    public void setPreCrashGForceTimeline(List<Float> preCrashGForceTimeline) { this.preCrashGForceTimeline = preCrashGForceTimeline; }

    // --- Parcelable Implementation (Corrected for Full API Compatibility) ---

    protected Accident(Parcel in) {
        accidentId = in.readString();
        userId = in.readString();
        long tmpTimestamp = in.readLong();
        timestamp = tmpTimestamp == -1 ? null : new Date(tmpTimestamp);
        latitude = in.readDouble();
        longitude = in.readDouble();
        impactMagnitude = in.readDouble();
        deltaV = in.readDouble();
        speedBeforeCrash = in.readFloat();
        severity = in.readString();
        vehicleType = in.readString();
        
        // Manually read the list to ensure compatibility
        int listSize = in.readInt();
        if (listSize != -1) {
            this.preCrashGForceTimeline = new ArrayList<>(listSize);
            for (int i = 0; i < listSize; i++) {
                this.preCrashGForceTimeline.add(in.readFloat());
            }
        } else {
            this.preCrashGForceTimeline = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accidentId);
        dest.writeString(userId);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(impactMagnitude);
        dest.writeDouble(deltaV);
        dest.writeFloat(speedBeforeCrash);
        dest.writeString(severity);
        dest.writeString(vehicleType);
        
        // Manually write the list to ensure compatibility
        if (preCrashGForceTimeline == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(preCrashGForceTimeline.size());
            for (Float val : preCrashGForceTimeline) {
                dest.writeFloat(val);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Accident> CREATOR = new Creator<Accident>() {
        @Override
        public Accident createFromParcel(Parcel in) {
            return new Accident(in);
        }

        @Override
        public Accident[] newArray(int size) {
            return new Accident[size];
        }
    };
}
