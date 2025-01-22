package com.example.mysecondclasshib.api;

import com.google.gson.annotations.SerializedName;

public class Game {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String released;

    @SerializedName("rating")
    private double rating;

    @SerializedName("background_image")
    private String backgroundImage;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReleased() {
        return released;
    }

    public double getRating() {
        return rating;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }
}