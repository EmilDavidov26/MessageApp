package com.example.mysecondclasshib.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private CircleImageView profileImage;
    private ImageButton editProfileImage;
    private TextView usernameDisplay;
    private TextView emailDisplay;
    private TextInputEditText descriptionEdit;
    private ChipGroup gamesChipGroup;
    private Button saveButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri imageUri;

    // Sample game list - you can replace this with API data
    private final List<String> availableGames = Arrays.asList(
            "Minecraft", "Fortnite", "Call of Duty", "GTA V", "League of Legends",
            "Valorant", "FIFA 23", "Among Us", "Roblox", "Apex Legends",
            "PUBG", "CS:GO", "Dota 2", "Overwatch", "Red Dead Redemption 2"
    );

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImage.setImageURI(imageUri);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserData();

        // Set click listeners
        editProfileImage.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveChanges());

        // Setup game chips
        setupGameChips();

        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        editProfileImage = view.findViewById(R.id.edit_profile_image);
        usernameDisplay = view.findViewById(R.id.username_display);
        emailDisplay = view.findViewById(R.id.email_display);
        descriptionEdit = view.findViewById(R.id.description_edit);
        gamesChipGroup = view.findViewById(R.id.games_chip_group);
        saveButton = view.findViewById(R.id.save_button);
    }

    private void setupGameChips() {
        gamesChipGroup.removeAllViews();

        for (String game : availableGames) {
            Chip chip = new Chip(requireContext());
            chip.setText(game);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setCloseIconVisible(false);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));

            gamesChipGroup.addView(chip);
        }

        // Limit selection to 5 games
        gamesChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 5) {
                // Uncheck the last selected chip
                Chip lastCheckedChip = group.findViewById(checkedIds.get(checkedIds.size() - 1));
                lastCheckedChip.setChecked(false);
                Toast.makeText(requireContext(), "You can only select up to 5 games",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usernameDisplay.setText(user.getUsername());
                    emailDisplay.setText(user.getEmail());
                    descriptionEdit.setText(user.getDescription());

                    // Load profile image
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage);
                    }

                    // Set selected games
                    List<String> favGames = user.getFavGames();
                    if (favGames != null) {
                        for (int i = 0; i < gamesChipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) gamesChipGroup.getChildAt(i);
                            chip.setChecked(favGames.contains(chip.getText().toString()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImage.launch(intent);
    }

    private void saveChanges() {
        String description = descriptionEdit.getText().toString().trim();

        // Get selected games
        List<String> selectedGames = new ArrayList<>();
        for (int i = 0; i < gamesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) gamesChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedGames.add(chip.getText().toString());
            }
        }

        if (imageUri != null) {
            // Upload image first
            StorageReference fileRef = storageRef.child(auth.getCurrentUser().getUid());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Save all data with image URL
                                updateProfile(description, uri.toString(), selectedGames);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        // Still save other data even if image upload fails
                        updateProfile(description, null, selectedGames);
                    });
        } else {
            // Just save description and games
            updateProfile(description, null, selectedGames);
        }
    }

    private void updateProfile(String description, String imageUrl, List<String> favGames) {
        DatabaseReference ref = userRef;

        if (imageUrl != null) {
            ref.child("imageUrl").setValue(imageUrl);
        }

        ref.child("description").setValue(description);
        ref.child("favGames").setValue(favGames)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                );
    }
}