package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.FriendsListAdapter;
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsListFragment extends Fragment implements FriendsListAdapter.OnFriendActionListener {
    private static final String TAG = "FriendsListFragment";
    private RecyclerView recyclerView;
    private FriendsListAdapter adapter;
    private List<User> friendsList;
    private TextView emptyView;
    private DatabaseReference usersRef;
    private String currentUserId;
    private ValueEventListener friendsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        recyclerView = view.findViewById(R.id.friends_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        friendsList = new ArrayList<>();

        adapter = new FriendsListAdapter(requireContext(), friendsList, this);
        recyclerView.setAdapter(adapter);

        loadFriends();

        return view;
    }

    private void loadFriends() {
        friendsListener = usersRef.child(currentUserId).child("friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        friendsList.clear();

                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            showEmptyView();
                            return;
                        }

                        for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                            String friendId = friendSnapshot.getKey();
                            if (friendId != null && friendSnapshot.getValue(Boolean.class)) {
                                loadFriendData(friendId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading friends: " + error.getMessage());
                        showEmptyView();
                    }
                });
    }

    private void loadFriendData(String friendId) {
        usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User friend = snapshot.getValue(User.class);
                if (friend != null) {
                    friendsList.add(friend);
                    adapter.notifyDataSetChanged();
                    showRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading friend data: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRemoveFriend(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUserId + "/friends/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friends/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show();
                    friendsList.remove(user);
                    adapter.notifyDataSetChanged();
                    if (friendsList.isEmpty()) {
                        showEmptyView();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing friend: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to remove friend", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onFriendClick(User user) {
        Bundle args = new Bundle();
        args.putString("userId", user.getId());
        args.putString("username", user.getUsername());
        Navigation.findNavController(requireView()).navigate(R.id.action_friends_to_chat, args);
    }

    private void showEmptyView() {
        if (recyclerView != null && emptyView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No friends yet");
        }
    }

    private void showRecyclerView() {
        if (recyclerView != null && emptyView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendsListener != null) {
            usersRef.child(currentUserId).child("friends").removeEventListener(friendsListener);
        }
    }
}