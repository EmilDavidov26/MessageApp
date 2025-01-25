package com.example.mysecondclasshib.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GameApiService {
    @GET("games")
    Call<GameResponse> getGames(
            @Query("key") String apiKey,
            @Query("page_size") int pageSize,
            @Query("ordering") String ordering,
            @Query("platforms") String platforms,
            @Query("search") String search
    );
}
