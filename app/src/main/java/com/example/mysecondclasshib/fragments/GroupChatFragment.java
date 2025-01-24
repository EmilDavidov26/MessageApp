package com.example.mysecondclasshib.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.mysecondclasshib.adapters.GroupMessageAdapter;
import com.example.mysecondclasshib.models.GroupMessage;
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private String groupId;
    private String groupName;
    private FirebaseAuth auth;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesListener;
    private List<GroupMessage> messages;
    private GroupMessageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            groupName = getArguments().getString("groupName");
        }

        initViews(view);
        setupRecyclerView();
        loadMessages();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_members) {
            showMembersDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(View view) {
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(groupName);

        recyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);

        auth = FirebaseAuth.getInstance();
        messagesRef = FirebaseDatabase.getInstance().getReference("group_messages").child(groupId);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new GroupMessageAdapter(requireContext(), messages, auth.getCurrentUser().getUid());
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        messagesListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMembersDialog() {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        groupRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> memberIds = new ArrayList<>();
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    if (memberSnapshot.getValue(Boolean.class)) {
                        memberIds.add(memberSnapshot.getKey());
                    }
                }
                showMembersList(memberIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showMembersList(List<String> memberIds) {
        List<String> memberNames = new ArrayList<>();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        AtomicInteger loadedCount = new AtomicInteger(0);

        for (String memberId : memberIds) {
            usersRef.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        memberNames.add(user.getUsername());
                    }
                    if (loadedCount.incrementAndGet() == memberIds.size()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Group Members");
                        builder.setItems(memberNames.toArray(new String[0]), null);
                        builder.setPositiveButton("Close", null);
                        builder.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (msg.isEmpty()) return;

        String messageId = messagesRef.push().getKey();
        if (messageId == null) return;

        GroupMessage message = new GroupMessage(messageId, auth.getCurrentUser().getUid(), msg);
        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> messageInput.setText(""))
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
}