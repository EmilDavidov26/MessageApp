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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context context;
    private List<User> usersList;
    private OnUserClickListener listener;
    private OnFriendActionListener friendListener;
    private String currentUserId;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public interface OnFriendActionListener {
        void onAddFriend(User user);
        void onAcceptRequest(User user);
        void onRemoveFriend(User user);
        void onCancelRequest(User user);
    }

    public UsersAdapter(Context context, List<User> usersList, OnUserClickListener listener,
                        OnFriendActionListener friendListener) {
        this.context = context;
        this.usersList = usersList;
        this.listener = listener;
        this.friendListener = friendListener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        // Set friend button state and click listener
        updateFriendButton(holder.friendButton, user);

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    private void updateFriendButton(Button button, User user) {
        // First check if users are already friends
        if (user.isFriend(currentUserId)) {
            button.setText("Friends");
            button.setOnClickListener(v -> friendListener.onRemoveFriend(user));
        }
        else {
            // Check if there are any pending requests
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("users");

            // First check if we received a request from this user
            requestRef.child(currentUserId)
                    .child("friendRequests")
                    .child(user.getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && "sent".equals(snapshot.getValue(String.class))) {
                                // We received a request from this user
                                button.setText("Accept");
                                button.setOnClickListener(v -> friendListener.onAcceptRequest(user));
                            } else {
                                // Check if we sent a request to this user
                                requestRef.child(user.getId())
                                        .child("friendRequests")
                                        .child(currentUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists() && "sent".equals(snapshot.getValue(String.class))) {
                                                    // We sent a request to this user
                                                    button.setText("Pending");
                                                    button.setOnClickListener(v -> friendListener.onCancelRequest(user));
                                                } else {
                                                    // No request exists
                                                    button.setText("Add Friend");
                                                    button.setOnClickListener(v -> friendListener.onAddFriend(user));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                button.setText("Add Friend");
                                                button.setOnClickListener(v -> friendListener.onAddFriend(user));
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            button.setText("Add Friend");
                            button.setOnClickListener(v -> friendListener.onAddFriend(user));
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView username;
        TextView description;
        Button friendButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username_text);
            description = itemView.findViewById(R.id.description_text);
            friendButton = itemView.findViewById(R.id.friend_button);
        }
    }
}