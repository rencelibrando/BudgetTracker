package com.expensetracker.budgettracker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.databinding.ActivityMainBinding;
import com.expensetracker.budgettracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "onCreate started");

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        long userId = sessionManager.getUserId();
        Log.d("MainActivity", "User ID: " + userId);

        try (DatabaseHelper databaseHelper = new DatabaseHelper(this)) {
            if (userId == -1 || databaseHelper.getUsername(userId) == null) {
                sessionManager.logoutUser();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }
        try {
            // Apply light/dark mode based on system settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                Log.d("MainActivity", "Using system default night mode");
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Log.d("MainActivity", "Defaulting to light mode");
            }

            // Initialize binding
            ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            Log.d("MainActivity", "Binding successful");

            // Set up Toolbar as the ActionBar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Log.d("MainActivity", "Toolbar set as ActionBar");

            // Set up navigation
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_transaction,
                    R.id.navigation_budget
            ).build();

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_activity_main);

            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                Log.d("MainActivity", "NavController initialized");

                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

                // BottomNavigationView setup
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                NavigationUI.setupWithNavController(bottomNavigationView, navController);

                // Add debug listener for BottomNavigationView
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    if (item.getItemId() == R.id.navigation_home) {
                        navController.navigate(R.id.navigation_home);
                        toolbar.setTitle("Home");
                    } else if (item.getItemId() == R.id.navigation_transaction) {
                        navController.navigate(R.id.navigation_transaction);
                        toolbar.setTitle("Transaction");
                    } else if (item.getItemId() == R.id.navigation_budget) {
                        navController.navigate(R.id.navigation_budget);
                        toolbar.setTitle("Budget");
                    } else {
                        return false;
                    }
                    return true;
                });

            } else {
                Log.e("MainActivity", "NavHostFragment is null. Ensure that nav_host_fragment_activity_main exists in the layout.");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.d("MainActivity", "onCreate completed");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
