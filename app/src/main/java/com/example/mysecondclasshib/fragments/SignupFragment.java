package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFragment extends Fragment {
    private EditText emailInput, passwordInput, usernameInput;
    private Button signupButton, goToLoginButton;
    private ProgressBar progressBar;
    private NavController navController;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        Log.d("SignupFragment", "onCreateView");

        // Initialize views
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        usernameInput = view.findViewById(R.id.username_input);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginButton = view.findViewById(R.id.goto_login_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Use the NavHostFragment to get the NavController
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Set click listeners
        signupButton.setOnClickListener(v -> signupUser());
        goToLoginButton.setOnClickListener(v -> navController.navigateUp());

        return view;
    }

    private void signupUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Check if username is available
        FirebaseDatabase.getInstance().getReference("usernames")
                .child(username) // Query the usernames node for this username
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            progressBar.setVisibility(View.GONE);
                            signupButton.setEnabled(true);
                            usernameInput.setError("Username already taken");
                            usernameInput.requestFocus();
                        } else {
                            // Username is available, proceed with registration
                            createAccount(email, password, username);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        Log.e("SignupFragment", "Error checking username", task.getException());
                        Toast.makeText(getContext(), "Error checking username", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createAccount(String email, String password, String username) {
        // Create the user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userId = firebaseUser.getUid();  // Get the Firebase UID for the user

                        // Log to confirm the userId and username
                        Log.d("SignupFragment", "userId: " + userId + ", username: " + username);

                        // Create user object with the userId, username, and email
                        User user = new User(userId, username, email);

                        // Save user data under the 'users' node in Firebase
                        usersRef.child(userId).setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    signupButton.setEnabled(true);

                                    if (dbTask.isSuccessful()) {
                                        // Proceed to the next fragment
                                        navController.navigate(R.id.action_signup_to_users);
                                    } else {
                                        String errorMessage = dbTask.getException() != null ?
                                                dbTask.getException().getMessage() : "Failed to create user profile";
                                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
