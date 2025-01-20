package com.example.mysecondclasshib.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersList;
    private List<User> allUsers;  // Store all users for filtering
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private String currentGameFilter = null;

    // Sample game list - should match the one in ProfileFragment
    private final List<String> availableGames = Arrays.asList(
            "Minecraft", "Fortnite", "Call of Duty", "GTA V", "League of Legends",
            "Valorant", "FIFA 23", "Among Us", "Roblox", "Apex Legends",
            "PUBG", "CS:GO", "Dota 2", "Overwatch", "Red Dead Redemption 2"
    );

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
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersList = new ArrayList<>();
        allUsers = new ArrayList<>();  // Initialize allUsers list

        adapter = new UsersAdapter(requireContext(), usersList, user -> {
            Bundle args = new Bundle();
            args.putString("userId", user.getId());
            args.putString("username", user.getUsername());
            Navigation.findNavController(view).navigate(R.id.action_users_to_chat, args);
        });

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
        else if (itemId == R.id.action_logout) {
            auth.signOut();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_login);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGameSelectionDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_search);

        RecyclerView gamesRecyclerView = dialog.findViewById(R.id.games_recycler_view);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        GameSelectionAdapter gameAdapter = new GameSelectionAdapter(availableGames, game -> {
            filterUsersByGame(game);
            dialog.dismiss();
        });

        gamesRecyclerView.setAdapter(gameAdapter);

        dialog.findViewById(R.id.clear_filter_button).setOnClickListener(v -> {
            clearGameFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterUsersByGame(String game) {
        currentGameFilter = game;
        usersList.clear();

        // Filter users who have the selected game
        usersList.addAll(allUsers.stream()
                .filter(user -> user.getFavGames() != null && user.getFavGames().contains(game))
                .collect(Collectors.toList()));

        // Sort users: online users first, then by username
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

    private void sortUsers() {
        usersList.sort((user1, user2) -> {
            if (user1.isOnline() != user2.isOnline()) {
                return user2.isOnline() ? 1 : -1;
            }
            return user1.getUsername().compareToIgnoreCase(user2.getUsername());
        });
        adapter.notifyDataSetChanged();
    }

    private void loadUsers() {
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserId = auth.getCurrentUser().getUid();
                allUsers.clear();
                usersList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getId() != null && !currentUserId.equals(user.getId())) {
                        allUsers.add(user);
                    }
                }

                // Apply current filter if exists, otherwise show all users
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
}