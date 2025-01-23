package com.example.mysecondclasshib.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.adapters.GameSelectionAdapter;
import com.example.mysecondclasshib.adapters.UsersAdapter;
import com.example.mysecondclasshib.api.GameRepository;
import com.example.mysecondclasshib.models.User;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.stream.Collectors;

public class UsersFragment extends Fragment implements UsersAdapter.OnFriendActionListener {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersList;
    private List<User> allUsers;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private String currentGameFilter = null;
    private String currentUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Set action bar title
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Users");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersList = new ArrayList<>();
        allUsers = new ArrayList<>();

        adapter = new UsersAdapter(
                requireContext(),
                usersList,
                user -> {
                    Bundle args = new Bundle();
                    args.putString("userId", user.getId());
                    args.putString("username", user.getUsername());
                    Navigation.findNavController(view).navigate(R.id.action_users_to_chat, args);
                },
                this
        );

        recyclerView.setAdapter(adapter);

        // Update current user's online status
        updateUserStatus(true);

        // Load users
        loadUsers();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_users, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            showGameSelectionDialog();
            return true;
        }
        else if (itemId == R.id.action_profile) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_profile);
            return true;
        }
        else if (itemId == R.id.action_friends) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_friends);
            return true;
        }
        else if (itemId == R.id.action_logout) {
            updateUserStatus(false);
            auth.signOut();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_login);
            return true;
        }
        else if (itemId == R.id.action_settings) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGameSelectionDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_search);

        // Set dialog width to 90% of screen width and fixed height
        Window window = dialog.getWindow();
        if (window != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RecyclerView gamesRecyclerView = dialog.findViewById(R.id.games_recycler_view);
        TextInputEditText searchEditText = dialog.findViewById(R.id.search_edit_text);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);

        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        GameSelectionAdapter gameAdapter = new GameSelectionAdapter(game -> {
            filterUsersByGame(game);
            dialog.dismiss();
        });
        gamesRecyclerView.setAdapter(gameAdapter);

        GameRepository gameRepository = new GameRepository();
        Handler searchHandler = new Handler();
        final long SEARCH_DELAY_MS = 500;

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    gameRepository.searchGames(s.toString(), 50, new GameRepository.OnGamesFetchedListener() {
                        @Override
                        public void onSuccess(List<String> games) {
                            requireActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                gameAdapter.setGames(games);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            requireActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(requireContext(),
                                        "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }, SEARCH_DELAY_MS);
            }
        });

        // Initial game load
        progressBar.setVisibility(View.VISIBLE);
        gameRepository.fetchGames(50, new GameRepository.OnGamesFetchedListener() {
            @Override
            public void onSuccess(List<String> games) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    gameAdapter.setGames(games);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error loading games: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        dialog.findViewById(R.id.clear_filter_button).setOnClickListener(v -> {
            clearGameFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterUsersByGame(String game) {
        currentGameFilter = game;
        usersList.clear();
        usersList.addAll(allUsers.stream()
                .filter(user ->
                        !user.getId().equals(currentUserId) &&
                                user.getFavGames() != null &&
                                user.getFavGames().contains(game))
                .collect(Collectors.toList()));
        sortUsers();

        String message = usersList.isEmpty() ?
                "No users found playing " + game :
                "Found " + usersList.size() + " users playing " + game;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearGameFilter() {
        currentGameFilter = null;
        usersList.clear();
        usersList.addAll(allUsers);
        sortUsers();
        Toast.makeText(requireContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadUsers() {
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                usersList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Set the user's ID from the snapshot key
                        user.setId(dataSnapshot.getKey());

                        // Only add if it's not the current user
                        if (!dataSnapshot.getKey().equals(currentUserId)) {
                            allUsers.add(user);
                        }
                    }
                }

                if (currentGameFilter != null) {
                    filterUsersByGame(currentGameFilter);
                } else {
                    usersList.addAll(allUsers);
                    sortUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addValueEventListener(usersListener);
    }

    private void sortUsers() {
        usersList.sort((user1, user2) -> {
            // First, sort by friend status
            boolean isFriend1 = user1.getFriends() != null && user1.getFriends().containsKey(currentUserId);
            boolean isFriend2 = user2.getFriends() != null && user2.getFriends().containsKey(currentUserId);
            if (isFriend1 != isFriend2) {
                return isFriend1 ? -1 : 1;
            }
            // Then by online status
            if (user1.isOnline() != user2.isOnline()) {
                return user2.isOnline() ? 1 : -1;
            }
            // Finally by username
            return user1.getUsername().compareToIgnoreCase(user2.getUsername());
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAddFriend(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + user.getId() + "/friendRequests/" + currentUserId, "sent");
        updates.put("/users/" + currentUserId + "/friendRequests/" + user.getId(), "received");

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to send request", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onAcceptRequest(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUserId + "/friends/" + user.getId(), true);
        updates.put("/users/" + user.getId() + "/friends/" + currentUserId, true);
        updates.put("/users/" + currentUserId + "/friendRequests/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friendRequests/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to accept request", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onRemoveFriend(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUserId + "/friends/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friends/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend removed",Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to remove friend", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onCancelRequest(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUserId + "/friendRequests/" + user.getId(), null);
        updates.put("/users/" + user.getId() + "/friendRequests/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Friend request cancelled", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to cancel request", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateUserStatus(boolean online) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRef = usersRef.child(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("online", online);
            if (!online) {
                updates.put("lastSeen", String.valueOf(System.currentTimeMillis()));
            }

            userRef.updateChildren(updates);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserStatus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateUserStatus(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
        updateUserStatus(false);
    }
    @Override
    public void onStop() {
        super.onStop();
        updateUserStatus(false);
    }
}