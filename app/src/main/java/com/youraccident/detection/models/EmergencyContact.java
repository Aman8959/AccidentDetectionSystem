package com.youraccident.detection.models;

public class EmergencyContact {
    private String name;
    private String phoneNumber;
    private boolean shouldCall;

    public EmergencyContact(String name, String phoneNumber, boolean shouldCall) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.shouldCall = shouldCall;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean shouldCall() {
        return shouldCall;
    }

    public void setShouldCall(boolean shouldCall) {
        this.shouldCall = shouldCall;
    }
}
