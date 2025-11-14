package com.youraccident.detection.models;

import java.util.Date;

public class Accident {
    private String accidentId;
    private String userId;
    private double latitude;
    private double longitude;
    private Date timestamp;
    private String status;

    // Constructors
    public Accident() {}

    public Accident(String userId, double latitude, double longitude) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = new Date();
        this.status = "DETECTED";
    }

    // Getters and Setters
    public String getAccidentId() { return accidentId; }
    public void setAccidentId(String accidentId) { this.accidentId = accidentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}