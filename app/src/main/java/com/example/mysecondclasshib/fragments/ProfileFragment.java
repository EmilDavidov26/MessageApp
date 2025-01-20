package com.example.mysecondclasshib.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private CircleImageView profileImage;
    private ImageButton editProfileImage;
    private TextInputEditText usernameEdit, bioEdit, emailEdit;
    private Button saveButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri imageUri;

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
        userRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        editProfileImage = view.findViewById(R.id.edit_profile_image);
        usernameEdit = view.findViewById(R.id.username_edit);
        bioEdit = view.findViewById(R.id.bio_edit);
        emailEdit = view.findViewById(R.id.email_edit);
        saveButton = view.findViewById(R.id.save_button);

        // Load user data
        loadUserData();

        // Set click listeners
        editProfileImage.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usernameEdit.setText(user.getUsername());
                    bioEdit.setText(user.getBio());
                    emailEdit.setText(user.getEmail());

                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImage.launch(intent);
    }

    private void saveUserData() {
        String username = usernameEdit.getText().toString().trim();
        String bio = bioEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEdit.setError("Username cannot be empty");
            return;
        }

        // Update user data
        userRef.child("username").setValue(username);
        userRef.child("bio").setValue(bio);
        userRef.child("email").setValue(email);

        // Upload new profile image if selected
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(auth.getCurrentUser().getUid());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                userRef.child("imageUrl").setValue(uri.toString());
                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}