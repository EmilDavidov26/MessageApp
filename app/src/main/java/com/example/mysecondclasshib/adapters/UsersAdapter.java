package com.example.mysecondclasshib.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context context;
    private List<User> usersList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UsersAdapter(Context context, List<User> usersList, OnUserClickListener listener) {
        this.context = context;
        this.usersList = usersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);

        // Set username
        holder.username.setText(user.getUsername());

        // Set description
        StringBuilder descriptionBuilder = new StringBuilder();

        // Add bio if exists
        if (user.getDescription() != null && !user.getDescription().trim().isEmpty()) {
            descriptionBuilder.append(user.getDescription());
        }

        // Add favorite games if exist
        List<String> favGames = user.getFavGames();
        if (favGames != null && !favGames.isEmpty()) {
            if (descriptionBuilder.length() > 0) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append("🎮 ").append(TextUtils.join(", ", favGames));
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

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView username;
        TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username_text);
            description = itemView.findViewById(R.id.description_text);
        }
    }
}