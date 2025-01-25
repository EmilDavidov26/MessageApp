package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mysecondclasshib.R;
import com.example.mysecondclasshib.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignupFragment extends Fragment {
    private static final String TAG = "SignupFragment";
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    private TextInputEditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signupButton, goToLoginButton;
    private ProgressBar progressBar;
    private NavController navController;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Initialize views
        initializeViews(view);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get NavController
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Set click listeners
        signupButton.setOnClickListener(v -> validateAndSignup());
        goToLoginButton.setOnClickListener(v -> navController.navigateUp());

        // Add text watchers for real-time validation
        setupTextWatchers();

        return view;
    }

    private void initializeViews(View view) {
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginButton = view.findViewById(R.id.goto_login_button);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupTextWatchers() {
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });

        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch();
            }
        });
    }

    private void validateAndSignup() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate username
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameInput.setError("Username must be at least 3 characters long");
            usernameInput.requestFocus();
            return;
        }

        // Validate email
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

        // Validate password
        if (!validatePassword(password)) {
            passwordInput.requestFocus();
            return;
        }

        // Validate password match
        if (!validatePasswordMatch()) {
            confirmPasswordInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Create the account
        createAccount(email, password, username);
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            passwordInput.setError("Password is not strong enough");
            return false;
        }

        return true;
    }

    private boolean validatePasswordMatch() {
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void createAccount(String email, String password, String username) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        User user = new User(userId, username, email);

                        usersRef.child(userId).setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    signupButton.setEnabled(true);

                                    if (dbTask.isSuccessful()) {
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