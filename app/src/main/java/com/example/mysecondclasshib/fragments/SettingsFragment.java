package com.example.mysecondclasshib.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mysecondclasshib.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SettingsFragment extends Fragment {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    private TextInputEditText emailCurrentPassword, newEmail, confirmNewEmail;
    private TextInputEditText currentPassword, newPassword, confirmNewPassword;
    private Button changeEmailButton, changePasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View view) {
        emailCurrentPassword = view.findViewById(R.id.email_current_password);
        newEmail = view.findViewById(R.id.new_email);
        confirmNewEmail = view.findViewById(R.id.confirm_new_email);
        currentPassword = view.findViewById(R.id.current_password);
        newPassword = view.findViewById(R.id.new_password);
        confirmNewPassword = view.findViewById(R.id.confirm_new_password);
        changeEmailButton = view.findViewById(R.id.change_email_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        progressBar = view.findViewById(R.id.progress_bar);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid());

        changeEmailButton.setOnClickListener(v -> updateEmail());
        changePasswordButton.setOnClickListener(v -> updatePassword());
    }


    private void updatePassword() {
        String currentPass = currentPassword.getText().toString();
        String newPass = newPassword.getText().toString();
        String confirmPass = confirmNewPassword.getText().toString();

        if (!validatePasswordInputs(currentPass, newPass, confirmPass)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = auth.getCurrentUser();

        // First reauthenticate
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPass)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        clearPasswordFields();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private boolean validateEmailInputs(String currentPassword, String newEmail, String confirmEmail) {
        if (currentPassword.isEmpty()) {
            emailCurrentPassword.setError("Current password is required");
            return false;
        }

        if (newEmail.isEmpty()) {
            this.newEmail.setError("New email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            this.newEmail.setError("Please enter a valid email");
            return false;
        }

        if (!newEmail.equals(confirmEmail)) {
            confirmNewEmail.setError("Emails do not match");
            return false;
        }

        return true;
    }

    private boolean validatePasswordInputs(String currentPass, String newPass, String confirmPass) {
        if (currentPass.isEmpty()) {
            currentPassword.setError("Current password is required");
            return false;
        }

        if (newPass.isEmpty()) {
            newPassword.setError("New password is required");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(newPass).matches()) {
            newPassword.setError("Password must contain at least 8 characters, including uppercase, lowercase, number and special character");
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            confirmNewPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void clearEmailFields() {
        emailCurrentPassword.setText("");
        newEmail.setText("");
        confirmNewEmail.setText("");
    }

    private void clearPasswordFields() {
        currentPassword.setText("");
        newPassword.setText("");
        confirmNewPassword.setText("");
    }
    private void updateEmail() {
        String currentPassword = emailCurrentPassword.getText().toString();
        String newEmailStr = newEmail.getText().toString().trim();
        String confirmEmailStr = confirmNewEmail.getText().toString().trim();

        if (!validateEmailInputs(currentPassword, newEmailStr, confirmEmailStr)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = auth.getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updateEmail(newEmailStr)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Update Firebase Realtime Database
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("email", newEmailStr);

                                        userRef.updateChildren(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                                                    clearEmailFields();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Failed to update database", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update email: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}