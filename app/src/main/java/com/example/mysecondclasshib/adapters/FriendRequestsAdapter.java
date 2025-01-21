package com.example.mysecondclasshib.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {
    private Context context;
    private List<User> requestsList;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(User user);
        void onDeclineRequest(User user);
    }

    public FriendRequestsAdapter(Context context, List<User> requestsList, OnRequestActionListener listener) {
        this.context = context;
        this.requestsList = requestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = requestsList.get(position);

        // Set username
        holder.username.setText(user.getUsername());

        // Set description and games
        StringBuilder descriptionBuilder = new StringBuilder();
        if (user.getDescription() != null && !user.getDescription().trim().isEmpty()) {
            descriptionBuilder.append(user.getDescription());
        }
        if (user.getFavGames() != null && !user.getFavGames().isEmpty()) {
            if (descriptionBuilder.length() > 0) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append("🎮 ").append(String.join(", ", user.getFavGames()));
        }

        String description = descriptionBuilder.toString();
        if (!description.isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(description);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Load profile image
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }

        // Set click listeners
        holder.acceptButton.setOnClickListener(v -> listener.onAcceptRequest(user));
        holder.declineButton.setOnClickListener(v -> listener.onDeclineRequest(user));
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView username;
        TextView description;
        Button acceptButton;
        Button declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username_text);
            description = itemView.findViewById(R.id.description_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
        }
    }
}