package com.youraccident.detection.models;

public class Trip {
    private long startTime;
    private long endTime;
    private float distance;
    private float maxSpeed;

    public Trip() {
        this.startTime = System.currentTimeMillis();
    }

    public void endTrip() {
        this.endTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public long getDuration() {
        return (endTime - startTime) / 1000; // in seconds
    }

    public float getAverageSpeed() {
        if (getDuration() > 0) {
            return distance / getDuration(); // m/s
        }
        return 0;
    }
}
