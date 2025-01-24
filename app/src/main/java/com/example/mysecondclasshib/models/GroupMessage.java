package com.example.mysecondclasshib.models;

public class GroupMessage {
    private String id;
    private String senderId;
    private String message;
    private long timestamp;

    public GroupMessage() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public GroupMessage(String id, String senderId, String message) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }


}