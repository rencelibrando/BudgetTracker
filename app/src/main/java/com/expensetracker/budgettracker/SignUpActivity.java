package com.expensetracker.budgettracker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class SignUpActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        databaseHelper = new DatabaseHelper(this);
        initializeUI();
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email); // Was R.id.email
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password); // Was R.id.confirm_password
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

        if (registerUser(username, email, password)) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            String hashedPassword = SecurityUtils.hashPassword(password);
            if (databaseHelper.isUserExists(db, username, email)) {
                Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT).show();
                return false;
            }

            long result = databaseHelper.createUser(db, username, email, hashedPassword);
            if (result != -1) {
                new SessionManager(SignUpActivity.this).loginUser(result, username);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("Signup", "Error: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(SignUpActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return false;
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}