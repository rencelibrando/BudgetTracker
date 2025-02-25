package com.expensetracker.budgettracker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.utils.SecurityUtils;
import com.expensetracker.budgettracker.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // For background tasks
    private final Handler handler = new Handler(Looper.getMainLooper()); // For UI updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        databaseHelper = new DatabaseHelper(this);
        initializeUI();
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        TextView tvLogin = findViewById(R.id.tv_login);

        btnSignUp.setOnClickListener(v -> attemptSignUp());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void attemptSignUp() {
        String username = etUsername.getText().toString().trim().toLowerCase();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords must match");
            return;
        }

        // Perform registration in the background
        executor.execute(() -> registerUser(username, email, password));
    }

    private void registerUser(String username, String email, String password) {
        SQLiteDatabase db = null;
        try {
            db = databaseHelper.getWritableDatabase();
            String hashedPassword = SecurityUtils.hashPassword(password);

            if (databaseHelper.isUserExists(db, username, email)) {
                showToast("Username or email already exists");
                return;
            }

            long result = databaseHelper.createUser(db, username, email, hashedPassword);
            if (result != -1) {
                new SessionManager(SignUpActivity.this).loginUser(result, username);
                showToast("Registration successful!");
                handler.post(this::navigateToLogin); // Navigate to login on the UI thread
            } else {
                showToast("Registration failed");
            }
        } catch (Exception e) {
            Log.e("Signup", "Error: " + e.getMessage());
            showToast("Registration failed: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close(); // Ensure the database is closed
            }
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showToast(String message) {
        handler.post(() -> Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        executor.shutdown(); // Shutdown the executor service
        databaseHelper.close();
        super.onDestroy();
    }
}