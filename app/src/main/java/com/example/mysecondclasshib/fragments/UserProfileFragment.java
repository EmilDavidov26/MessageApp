package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment {
    private CircleImageView profileImage;
    private TextView usernameDisplay;
    private TextView descriptionText;
    private ChipGroup gamesChipGroup;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        initializeViews(view);
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        usernameDisplay = view.findViewById(R.id.username_display);
        descriptionText = view.findViewById(R.id.description_text);
        gamesChipGroup = view.findViewById(R.id.games_chip_group);
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Set username
                    usernameDisplay.setText(user.getUsername());

                    // Set description
                    descriptionText.setText(user.getDescription());

                    // Load profile image
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage);
                    }

                    // Set favorite games
                    setupGameChips(user.getFavGames());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGameChips(List<String> games) {
        gamesChipGroup.removeAllViews();

        for (String game : games) {
            Chip chip = new Chip(requireContext());
            chip.setText(game);
            chip.setClickable(false);
            chip.setCheckable(false);
            gamesChipGroup.addView(chip);
        }
    }
}