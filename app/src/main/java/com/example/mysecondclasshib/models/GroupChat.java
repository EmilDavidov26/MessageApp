package com.example.mysecondclasshib.models;

import java.util.HashMap;
import java.util.Map;

public class GroupChat {
    private String id;
    private String name;
    private String imageUrl;
    private Map<String, Boolean> members;
    private String createdBy;
    private long createdAt;

    public GroupChat() {}

    public GroupChat(String id, String name, String createdBy) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.members = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id != null ? id : ""; }
    public String getName() { return name != null ? name : ""; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public Map<String, Boolean> getMembers() { return members != null ? members : new HashMap<>(); }
    public String getCreatedBy() { return createdBy != null ? createdBy : ""; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMembers(Map<String, Boolean> members) { this.members = members; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}