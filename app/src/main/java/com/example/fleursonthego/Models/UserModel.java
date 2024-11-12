package com.example.fleursonthego.Models;

public class UserModel {
    private String name;
    private String phoneNumber;
    private String profileImageUrl;
    private String userType; // New field for userType

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId; // New field for userId

    // Default constructor (required for Firebase)
    public UserModel() { }

    // Constructor including userType
    public UserModel(String name, String phoneNumber, String profileImageUrl, String userType, String userId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.userType = userType; // Initialize userType
        this.userId = userId; // Initialize userId
    }

    // Getters and setters for name, phone number, profileImageUrl
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Getter and setter for userType
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
