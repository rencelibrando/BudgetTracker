package com.expensetracker.budgettracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.util.Pair;

public class LoginActivity extends AppCompatActivity {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeUI();
        checkExistingSession();
    }

    private void checkExistingSession() {
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startMainActivity();
        }
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        TextView tvSignUp = findViewById(R.id.tv_sign_up);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        btnLogin.setEnabled(false);
        executor.execute(() -> {
            Pair<Long, String> result = validateCredentials(username, password);
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                if (result != null) {
                    SessionManager session = new SessionManager(LoginActivity.this);
                    session.loginUser(result.first, result.second);
                    startMainActivity();
                } else {
                    showAuthError();
                }
            });
        });
    }

    private Pair<Long, String> validateCredentials(String username, String password) {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Pair<Long, String> result = null;

        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_USER_ID,
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_PASSWORD
                },
                DatabaseHelper.COLUMN_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username},
                null, null, null
        )) {
            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
                if (SecurityUtils.checkPassword(password, storedHash)) {
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                    String storedUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
                    result = new Pair<>(userId, storedUsername);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Authentication error", Toast.LENGTH_SHORT).show());
        } finally {
            db.close();
            dbHelper.close();
        }
        return result;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showAuthError() {
        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        etPassword.getText().clear();
    }

    private void navigateToSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}