package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.ChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.mysecondclasshib.models.Message;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Message> messagesList;
    private EditText messageInput;
    private ImageButton sendButton;
    private FirebaseAuth auth;
    private DatabaseReference chatRef;
    private String currentUserId, otherUserId;
    private ValueEventListener messagesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Get user IDs
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        Bundle args = getArguments();
        if (args != null) {
            otherUserId = args.getString("userId");
            String username = args.getString("username");
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(username);
        }

        // Initialize views
        recyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messagesList = new ArrayList<>();
        adapter = new ChatAdapter(getContext(),messagesList,currentUserId);
        recyclerView.setAdapter(adapter);

        // Set up Firebase
        String chatId = getChatId(currentUserId, otherUserId);
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Load messages
        loadMessages();

        return view;
    }

    private void loadMessages() {
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        Log.d("ChatFragment", "Message ID: " + message.getId());
                        Log.d("ChatFragment", "Message Seen: " + message.isSeen());
                        messagesList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();

                // Only scroll if we have messages
                if (!messagesList.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }

                // Mark messages as seen
                updateMessageSeen();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        chatRef.addValueEventListener(messagesListener);
    }
    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();

        // Check if otherUserId is correctly set
        Log.d("Chat", "Sending message to: " + otherUserId);  // Log the receiver's ID

        if (!msg.isEmpty()) {
            if (otherUserId != null && !otherUserId.isEmpty()) {
                Message message = new Message(currentUserId, otherUserId, msg);

                String messageId = chatRef.push().getKey();
                if (messageId != null) {
                    message.setId(messageId);

                    chatRef.child(messageId).setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                messageInput.setText("");
                                //sendNotification(msg);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Log.d("Chat", "Error: otherUserId is null or empty");
                Toast.makeText(getContext(), "Receiver ID is missing", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateMessageSeen() {
        for (Message message : messagesList) {
            if (!message.isSeen() && message.getSenderId().equals(otherUserId)) {
                String messageId = message.getId();
                if (messageId != null) {
                    chatRef.child(messageId).child("seen").setValue(true);
                }
            }
        }
    }

    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ?
                user1 + "_" + user2 : user2 + "_" + user1;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            chatRef.removeEventListener(messagesListener);
        }
    }
}