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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.FriendRequestsAdapter;
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

public class FriendRequestsFragment extends Fragment implements FriendRequestsAdapter.OnRequestActionListener {
    private static final String TAG = "FriendRequestsFragment";
    private RecyclerView recyclerView;
    private FriendRequestsAdapter adapter;
    private List<User> requestsList;
    private TextView emptyView;
    private DatabaseReference usersRef;
    private String currentUserId;
    private ValueEventListener requestsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        recyclerView = view.findViewById(R.id.requests_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        requestsList = new ArrayList<>();

        adapter = new FriendRequestsAdapter(requireContext(), requestsList, this);
        recyclerView.setAdapter(adapter);

        loadFriendRequests();

        return view;
    }

    private void loadFriendRequests() {
        DatabaseReference userRef = usersRef.child(currentUserId);

        requestsListener = userRef.child("friendRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsList.clear();

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    showEmptyView();
                    return;
                }

                // Iterate through friend requests
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String senderId = requestSnapshot.getKey();
                    String requestType = requestSnapshot.getValue(String.class);

                    // Only load requests that were sent to the current user
                    if (senderId != null && "sent".equals(requestType)) {
                        // Load the sender's user data
                        loadRequesterData(senderId);
                    }
                }

                if (requestsList.isEmpty()) {
                    showEmptyView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading friend requests: " + error.getMessage());
                showEmptyView();
            }
        });
    }

    private void loadRequesterData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    requestsList.add(user);
                    adapter.notifyDataSetChanged();
                    showRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading requester data: " + error.getMessage());
            }
        });
    }

    @Override
    public void onAcceptRequest(User user) {
        Map<String, Object> updates = new HashMap<>();
        // Add to friends lists
        updates.put("/users/" + currentUserId + "/friends/" + user.getId(), true);
        updates.put("/users/" + user.getId() + "/friends/" + currentUserId, true);

        // Remove friend requests
        updates.put("/users/" + currentUserId + "/friendRequests/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friendRequests/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                    requestsList.remove(user);
                    adapter.notifyDataSetChanged();
                    if (requestsList.isEmpty()) {
                        showEmptyView();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting request: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to accept request", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeclineRequest(User user) {
        Map<String, Object> updates = new HashMap<>();
        // Remove friend requests from both users
        updates.put("/users/" + currentUserId + "/friendRequests/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friendRequests/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
                    requestsList.remove(user);
                    adapter.notifyDataSetChanged();
                    if (requestsList.isEmpty()) {
                        showEmptyView();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error declining request: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to decline request", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEmptyView() {
        if (recyclerView != null && emptyView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No friend requests");
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
        if (requestsListener != null) {
            usersRef.child(currentUserId).removeEventListener(requestsListener);
        }
    }
}