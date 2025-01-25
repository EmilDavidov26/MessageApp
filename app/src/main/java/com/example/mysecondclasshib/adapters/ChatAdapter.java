package com.example.mysecondclasshib.adapters;

import com.example.mysecondclasshib.models.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context context;
    private List<Message> messages;
    private String currentUserId;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    public ChatAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_private_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_private_chat_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getMessage());
        holder.messageTime.setText(getFormattedTime(message.getTimestamp()));

        //  null check for seenText
        if (holder.seenText != null) {
            if (message.isSeen() && message.getSenderId().equals(currentUserId)) {
                holder.seenText.setVisibility(View.VISIBLE);
                holder.seenText.setText("Seen");
            } else {
                holder.seenText.setVisibility(View.GONE);
            }
        }
    }

    private String getFormattedTime(String timestamp) {
        try {
            long timeInMillis = Long.parseLong(timestamp);
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timeInMillis));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;
        TextView seenText;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            seenText = itemView.findViewById(R.id.seen_text);
        }
    }
}