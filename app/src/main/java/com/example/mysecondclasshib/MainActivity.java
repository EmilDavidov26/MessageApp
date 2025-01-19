package com.example.mysecondclasshib;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth auth;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate - after setContentView");

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Sign out the user to always prompt login
        auth.signOut();

        // Initialize NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            Log.d(TAG, "NavController initialized");
        } else {
            Log.e(TAG, "NavHostFragment is null!");
        }

        // Immediately navigate to login fragment
        navigateToLogin();
    }

    private void navigateToLogin() {
        if (navController == null) {
            Log.e(TAG, "NavController is not initialized!");
            return;
        }

        // Force navigation to login fragment
        Log.d(TAG, "Navigating to login fragment");
        navController.navigate(R.id.loginFragment);
    }
}
