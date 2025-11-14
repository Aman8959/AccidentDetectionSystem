package com.youraccident.detection.models;

public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String emergencyContact1;
    private String emergencyContact2;

    // Default constructor required for Gson
    public User() {
    }

    // Basic constructor
    public User(String id, String username, String email, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    // Full constructor
    public User(String id, String username, String email, String phone,
                String emergencyContact1, String emergencyContact2) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.emergencyContact1 = emergencyContact1;
        this.emergencyContact2 = emergencyContact2;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmergencyContact1() {
        return emergencyContact1 != null ? emergencyContact1 : "";
    }

    public void setEmergencyContact1(String emergencyContact1) {
        this.emergencyContact1 = emergencyContact1;
    }

    public String getEmergencyContact2() {
        return emergencyContact2 != null ? emergencyContact2 : "";
    }

    public void setEmergencyContact2(String emergencyContact2) {
        this.emergencyContact2 = emergencyContact2;
    }

    // Utility method to check if user has emergency contacts
    public boolean hasEmergencyContacts() {
        return (emergencyContact1 != null && !emergencyContact1.isEmpty()) ||
                (emergencyContact2 != null && !emergencyContact2.isEmpty());
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", emergencyContact1='" + emergencyContact1 + '\'' +
                ", emergencyContact2='" + emergencyContact2 + '\'' +
                '}';
    }
}