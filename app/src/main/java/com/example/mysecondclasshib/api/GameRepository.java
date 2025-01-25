package com.example.mysecondclasshib.api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
import java.util.stream.Collectors;

public class GameRepository {
    private static final String BASE_URL = "https://api.rawg.io/api/";
    private static final String API_KEY = "19cd78fc722749b291ec01be0cb00f13"; // My RAWG API key
    private static final String PLATFORMS = "4,187"; // PC and PS5
    private final GameApiService apiService;
    private Call<GameResponse> currentCall;

    public GameRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GameApiService.class);
    }

    public void searchGames(String query, int pageSize, OnGamesFetchedListener listener) {
        if (currentCall != null) {
            currentCall.cancel();
        }

        currentCall = apiService.getGames(API_KEY, pageSize, "-rating", PLATFORMS, query);
        currentCall.enqueue(new Callback<GameResponse>() {
            @Override
            public void onResponse(Call<GameResponse> call, Response<GameResponse> response) {
                if (call.isCanceled()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<String> gameNames = response.body().getResults()
                            .stream()
                            .map(Game::getName)
                            .collect(Collectors.toList());
                    listener.onSuccess(gameNames);
                } else {
                    listener.onError("Failed to fetch games: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameResponse> call, Throwable t) {
                if (call.isCanceled()) return;
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchGames(int pageSize, OnGamesFetchedListener listener) {
        searchGames("", pageSize, listener);
    }

    public interface OnGamesFetchedListener {
        void onSuccess(List<String> gameNames);
        void onError(String error);
    }
}