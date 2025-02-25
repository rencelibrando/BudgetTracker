package com.expensetracker.budgettracker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.databinding.ActivityMainBinding;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;
import com.expensetracker.budgettracker.ui.home.HomeViewModel;
import com.expensetracker.budgettracker.ui.home.HomeViewModelFactory;
import com.expensetracker.budgettracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        // Initialize binding first to avoid memory leaks
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check session and user validity
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Validate user data in background
        executor.execute(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            try {
                long userId = sessionManager.getUserId();
                boolean isValidUser = userId != -1 && dbHelper.getUsername(userId) != null;

                runOnUiThread(() -> {
                    if (!isValidUser) {
                        sessionManager.logoutUser();
                        navigateToLogin();
                    } else {
                        initializeUI(binding, sessionManager);
                    }
                });
            } finally {
                dbHelper.close();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeUI(@NonNull ActivityMainBinding binding, SessionManager sessionManager) {
        try {
            setupTheme();
            setupToolbar(binding.toolbar);
            setupNavigation(binding);
            setupViewModels();
            Log.d(TAG, "UI initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI", e);
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupNavigation(@NonNull ActivityMainBinding binding) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment != null
                ? navHostFragment.getNavController()
                : null;

        if (navController != null) {
            AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_transaction)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener((controller, destination, args) -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(destination.getLabel());
                }
            });
        } else {
            Log.e(TAG, "NavController not found");
            finish();
        }
    }

    private void setupViewModels() {
        TransactionViewModel transactionVM = new ViewModelProvider(this).get(TransactionViewModel.class);
        HomeViewModelFactory factory = new HomeViewModelFactory(transactionVM);
        HomeViewModel homeVM = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        // Add ViewModel observers if needed
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        return navHostFragment != null
                ? navHostFragment.getNavController().navigateUp()
                : super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}