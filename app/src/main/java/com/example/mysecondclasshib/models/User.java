package com.example.mysecondclasshib.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String imageUrl;
    private String description;

    private List<String> favGames;

    private boolean online;
    private String lastSeen;

    // Required empty constructor for Firebase
    public User(String userId, String username) {
    }
    public User(){
        this.favGames = new ArrayList<>();
    }

    // Constructor with id, username, and email (used during user registration)
    public User(String id, String username, String email) {
        this.id = id != null ? id : "";
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.phone = "";
        this.imageUrl = "";
        this.description = "";
        this.favGames = new ArrayList<>();
        this.online = false;
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

    public String getDescription() {  // Added description getter
        return description != null ? description : "";
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

    public void setDescription(String description) {  // Added description setter
        this.description = description != null ? description : "";
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen != null ? lastSeen : "";
    }
    public List<String> getFavGames() {
        return favGames != null ? favGames : new ArrayList<>();
    }

    public void setFavGames(List<String> favGames) {
        this.favGames = favGames != null ? favGames : new ArrayList<>();
    }
}