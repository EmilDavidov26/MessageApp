package com.example.mysecondclasshib.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.FriendSelectionAdapter;
import com.example.mysecondclasshib.adapters.GroupChatsAdapter;
import com.example.mysecondclasshib.models.GroupChat;
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GroupChatsFragment extends Fragment {
    private AlertDialog dialog;
    private RecyclerView recyclerView;
    private GroupChatsAdapter adapter;
    private List<GroupChat> groupList;
    private FirebaseAuth auth;
    private DatabaseReference groupsRef;
    private View dialogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chats, container, false);

        auth = FirebaseAuth.getInstance();
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        String currentUserId = auth.getCurrentUser().getUid();

        recyclerView = view.findViewById(R.id.groups_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupList = new ArrayList<>();

        adapter = new GroupChatsAdapter(requireContext(), groupList, group -> {
            Bundle args = new Bundle();
            args.putString("groupId", group.getId());
            args.putString("groupName", group.getName());
            Navigation.findNavController(view).navigate(R.id.action_groupChats_to_groupChat, args);
        });

        recyclerView.setAdapter(adapter);
        loadGroups(currentUserId);

        view.findViewById(R.id.create_group_fab).setOnClickListener(v -> showCreateGroupDialog());

        return view;
    }

    private void loadGroups(String userId) {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupChat group = dataSnapshot.getValue(GroupChat.class);
                    if (group != null && group.getMembers() != null && group.getMembers().containsKey(userId)) {
                        group.setId(dataSnapshot.getKey());
                        groupList.add(group);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateGroupDialog() {
        dialogView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        EditText groupNameInput = dialogView.findViewById(R.id.group_name_input);
        RecyclerView friendsRecyclerView = dialogView.findViewById(R.id.friends_recycler_view);

        dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create Group")
                .setView(dialogView)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String groupName = groupNameInput.getText().toString().trim();
                if (groupName.isEmpty()) {
                    groupNameInput.setError("Group name required");
                    return;
                }
                createGroup(groupName, getSelectedFriends());
                dialog.dismiss();
            });
        });

        loadFriends(friendsRecyclerView);
        dialog.show();
    }

    private void createGroup(String name, List<String> members) {
        String groupId = groupsRef.push().getKey();
        if (groupId == null) return;

        GroupChat group = new GroupChat(groupId, name, auth.getCurrentUser().getUid());

        group.getMembers().put(auth.getCurrentUser().getUid(), true);
        for (String memberId : members) {
            group.getMembers().put(memberId, true);
        }

        groupsRef.child(groupId).setValue(group)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Group created", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show());
    }

    private List<String> getSelectedFriends() {
        RecyclerView friendsRecyclerView = dialogView.findViewById(R.id.friends_recycler_view);
        if (friendsRecyclerView != null && friendsRecyclerView.getAdapter() instanceof FriendSelectionAdapter) {
            FriendSelectionAdapter adapter = (FriendSelectionAdapter) friendsRecyclerView.getAdapter();
            return adapter.getSelectedFriends();
        }
        return new ArrayList<>();
    }

    private void loadFriends(RecyclerView recyclerView) {
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        List<User> friendsList = new ArrayList<>();

        usersRef.child(currentUserId).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null && friendSnapshot.getValue(Boolean.class)) {
                        usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User friend = userSnapshot.getValue(User.class);
                                if (friend != null) {
                                    friend.setId(friendId);
                                    friendsList.add(friend);
                                    setupFriendSelection(recyclerView, friendsList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupFriendSelection(RecyclerView recyclerView, List<User> friends) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        FriendSelectionAdapter adapter = new FriendSelectionAdapter(requireContext(), friends);
        recyclerView.setAdapter(adapter);
    }
}
