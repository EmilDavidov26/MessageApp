package com.example.mysecondclasshib.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String email;
    private String imageUrl;
    private String description;
    private String bio;
    private List<String> favGames;
    private boolean online;
    private String lastSeen;
    private Map<String, Object> friends;        // Changed to Object to handle both Boolean and Map
    private Map<String, Object> friendRequests; // Changed to Object to handle both Boolean and Map

    // Required empty constructor for Firebase
    public User() {
        this.favGames = new ArrayList<>();
        this.friends = new HashMap<>();
        this.friendRequests = new HashMap<>();
    }

    public User(String id, String username, String email) {
        this.id = id != null ? id : "";
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.imageUrl = "";
        this.description = "";
        this.bio = "";
        this.favGames = new ArrayList<>();
        this.friends = new HashMap<>();
        this.friendRequests = new HashMap<>();
        this.online = false;
        this.lastSeen = String.valueOf(System.currentTimeMillis());
    }

    // Getters
    public String getId() { return id != null ? id : ""; }
    public String getUsername() { return username != null ? username : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public String getDescription() {
        if (description != null && !description.isEmpty()) return description;
        return bio != null ? bio : "";
    }
    public String getBio() { return bio != null ? bio : ""; }
    public List<String> getFavGames() { return favGames != null ? favGames : new ArrayList<>(); }
    public boolean isOnline() { return online; }
    public String getLastSeen() { return lastSeen != null ? lastSeen : ""; }

    public Map<String, Object> getFriends() {
        return friends != null ? friends : new HashMap<>();
    }

    public Map<String, Object> getFriendRequests() {
        return friendRequests != null ? friendRequests : new HashMap<>();
    }

    // Setters
    public void setId(String id) { this.id = id != null ? id : ""; }
    public void setUsername(String username) { this.username = username != null ? username : ""; }
    public void setEmail(String email) { this.email = email != null ? email : ""; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl != null ? imageUrl : ""; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
    public void setBio(String bio) { this.bio = bio != null ? bio : ""; }
    public void setFavGames(List<String> favGames) { this.favGames = favGames != null ? favGames : new ArrayList<>(); }
    public void setOnline(boolean online) { this.online = online; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen != null ? lastSeen : ""; }
    public void setFriends(Map<String, Object> friends) { this.friends = friends != null ? friends : new HashMap<>(); }
    public void setFriendRequests(Map<String, Object> requests) { this.friendRequests = requests != null ? requests : new HashMap<>(); }

    // Helper methods
    public boolean isFriend(String userId) {
        if (friends == null) return false;
        Object value = friends.get(userId);
        return value != null && (value instanceof Boolean ? (Boolean)value : true);
    }

    public boolean hasPendingRequest(String userId) {
        if (friendRequests == null) return false;
        Object value = friendRequests.get(userId);
        return value != null && (value instanceof Boolean ? (Boolean)value : true);
    }
}