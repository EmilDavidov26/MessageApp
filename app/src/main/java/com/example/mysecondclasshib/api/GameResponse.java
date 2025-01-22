/***** GameResponse.java *****/
package com.example.mysecondclasshib.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GameResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Game> results;

    public List<Game> getResults() {
        return results;
    }
}
