package com.example.mysecondclasshib.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String id;
    private String username;
    private String email;
    private String imageUrl;
    private String description;
    private List<String> favGames;
    private Boolean online;
    private String lastSeen;
    private Object friends;
    private Object friendRequests;

    public User() {
        this.favGames = new ArrayList<>();
    }


    public User(String id, String username, String email) {
        this.id = id != null ? id : "";
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.imageUrl = "";
        this.description = "";
        this.favGames = new ArrayList<>();
        this.online = false;
        this.lastSeen = String.valueOf(System.currentTimeMillis());
    }

    public String getId() { return id != null ? id : ""; }
    public String getUsername() { return username != null ? username : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public String getDescription() {
        return description != null ? description : "";
    }

    public List<String> getFavGames() { return favGames != null ? favGames : new ArrayList<>(); }
    public boolean isOnline() {
        return online != null ? online : false;
    }
    public String getLastSeen() { return lastSeen != null ? lastSeen : ""; }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFriends() {
        if (friends instanceof Map) {
            return (Map<String, Object>) friends;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFriendRequests() {
        if (friendRequests instanceof Map) {
            return (Map<String, Object>) friendRequests;
        }
        return new HashMap<>();
    }

    public void setId(String id) { this.id = id != null ? id : ""; }
    public void setUsername(String username) { this.username = username != null ? username : ""; }
    public void setEmail(String email) { this.email = email != null ? email : ""; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl != null ? imageUrl : ""; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    public void setFavGames(List<String> favGames) { this.favGames = favGames != null ? favGames : new ArrayList<>(); }
    public void setOnline(boolean online) { this.online = online; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen != null ? lastSeen : ""; }
    public void setFriends(Object friends) { this.friends = friends; }
    public void setFriendRequests(Object requests) { this.friendRequests = requests; }

    public boolean isFriend(String userId) {
        if (friends == null) return false;
        if (friends instanceof Map) {
            return ((Map<?, ?>) friends).containsKey(userId);
        }
        return false;
    }

    public boolean hasPendingRequest(String userId) {
        if (friendRequests == null) return false;
        if (friendRequests instanceof Map) {
            return ((Map<?, ?>) friendRequests).containsKey(userId);
        }
        return false;
    }
}