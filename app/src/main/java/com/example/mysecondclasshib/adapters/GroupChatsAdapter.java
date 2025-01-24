package com.example.mysecondclasshib.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.GroupChat;
import com.google.firebase.database.core.Context;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.ViewHolder> {
    private android.content.Context mContext;
    private List<GroupChat> groupList;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(GroupChat group);
    }

    public GroupChatsAdapter(android.content.Context context, List<GroupChat> groupList, OnGroupClickListener listener) {
        this.mContext = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupChat group = groupList.get(position);
        holder.groupName.setText(group.getName());
        holder.memberCount.setText(group.getMembers().size() + " members");

        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
            Glide.with(mContext)
                    .load(group.getImageUrl())
                    .placeholder(R.drawable.default_group)
                    .into(holder.groupImage);
        }

        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupImage;
        TextView groupName;
        TextView memberCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.group_image);
            groupName = itemView.findViewById(R.id.group_name);
            memberCount = itemView.findViewById(R.id.member_count);
        }
    }
}