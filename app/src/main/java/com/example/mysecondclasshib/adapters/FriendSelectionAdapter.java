package com.example.mysecondclasshib.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



import de.hdodenhof.circleimageview.CircleImageView;


import de.hdodenhof.circleimageview.CircleImageView;


public class FriendSelectionAdapter extends RecyclerView.Adapter<FriendSelectionAdapter.ViewHolder> {
    private Context mContext;
    private List<User> friends;
    private Set<String> selectedFriends;

    public FriendSelectionAdapter(android.content.Context context, List<User> friends) {
        this.mContext = context;
        this.friends = friends;
        this.selectedFriends = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_friend_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.userName.setText(friend.getUsername());

        if (friend.getImageUrl() != null && !friend.getImageUrl().isEmpty()) {
            Glide.with(mContext)
                    .load(friend.getImageUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.userImage);
        }

        holder.checkBox.setChecked(selectedFriends.contains(friend.getId()));
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            if (holder.checkBox.isChecked()) {
                selectedFriends.add(friend.getId());
            } else {
                selectedFriends.remove(friend.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public List<String> getSelectedFriends() {
        return new ArrayList<>(selectedFriends);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        TextView userName;
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            checkBox = itemView.findViewById(R.id.selection_checkbox);
        }
    }
}
