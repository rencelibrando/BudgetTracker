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

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
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
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvSignUp = findViewById(R.id.tv_sign_up);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private Long validateCredentials(String username, String password) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_PASSWORD
        };

        final String selection = DatabaseHelper.COLUMN_USERNAME + " = ? COLLATE NOCASE";
        final String[] selectionArgs = {username};

        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
                if (SecurityUtils.checkPassword(password, storedHash)) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void attemptLogin() {
        String usernameInput = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usernameInput)) {
            etUsername.setError("Username required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        try {
            Long userId = validateCredentials(usernameInput, password);
            if (userId != null && userId != -1L) {
                String storedUsername = databaseHelper.getUsername(userId);
                if (storedUsername != null) {
                    SessionManager session = new SessionManager(this);
                    session.loginUser(userId, storedUsername);
                    startMainActivity();
                } else {
                    showAuthError();
                }
            } else {
                showAuthError();
            }
        } catch (Exception e) {
            showAuthError();
        }
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
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}