package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.UsersAdapter;
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> friendsList;
    private TextView emptyView;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        recyclerView = view.findViewById(R.id.friends_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        friendsList = new ArrayList<>();

        adapter = new UsersAdapter(
                requireContext(),
                friendsList,
                user -> {
                    // Navigate to chat when clicking a friend
                    Bundle args = new Bundle();
                    args.putString("userId", user.getId());
                    args.putString("username", user.getUsername());
                    Navigation.findNavController(view).navigate(R.id.action_friends_to_chat, args);
                },
                null  // We don't need friend actions in the friends list
        );

        recyclerView.setAdapter(adapter);
        loadFriends();

        return view;
    }

    private void loadFriends() {
        usersRef.child(currentUserId).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                if (!friendsSnapshot.exists()) {
                    showEmptyView();
                    return;
                }

                friendsList.clear();
                for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null) {
                        loadFriendData(friendId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                // Handle error
            }
        });
    }

    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
}