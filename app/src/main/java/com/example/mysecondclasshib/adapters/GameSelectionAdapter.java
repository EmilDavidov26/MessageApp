package com.example.mysecondclasshib.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GameSelectionAdapter extends RecyclerView.Adapter<GameSelectionAdapter.ViewHolder> {
    private List<String> games;
    private final OnGameSelectedListener listener;

    public interface OnGameSelectedListener {
        void onGameSelected(String game);
    }

    public GameSelectionAdapter(OnGameSelectedListener listener) {
        this.games = new ArrayList<>();
        this.listener = listener;
    }

    public void setGames(List<String> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String game = games.get(position);
        holder.textView.setText(game);
        holder.itemView.setOnClickListener(v -> listener.onGameSelected(game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }
}