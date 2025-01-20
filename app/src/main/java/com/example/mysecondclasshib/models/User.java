package com.example.mysecondclasshib.models;

public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String imageUrl;
    private String bio;
    private boolean online;
    private String lastSeen;

    // Required empty constructor for Firebase
    public User(String userId, String username) {
    }
    public User(){}

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // Constructor with id, username, and email (used during user registration)
    public User(String id, String username, String email) {
        this.id = id != null ? id : "";  // Initialize id with the provided id
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.phone = "";
        this.imageUrl = "";
        this.online = false;
        this.bio="";
        this.lastSeen = String.valueOf(System.currentTimeMillis());
    }

    // Getters
    public String getId() {
        return id != null ? id : "";
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public boolean isOnline() {
        return online;
    }

    public String getLastSeen() {
        return lastSeen != null ? lastSeen : "";
    }

    // Setters
    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen != null ? lastSeen : "";
    }
}
