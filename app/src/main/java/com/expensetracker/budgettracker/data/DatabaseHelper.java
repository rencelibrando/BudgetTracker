package com.expensetracker.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BudgetTracker.db";
    private static final int DATABASE_VERSION = 2;

    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TYPE = "type";

    // Budgets Table
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_ID = "budget_id";
    public static final String COLUMN_BUDGET_AMOUNT = "budget_amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);");

        // Create Transactions table
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // Create Budgets table
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_CATEGORY + " TEXT UNIQUE NOT NULL, " +
                COLUMN_BUDGET_AMOUNT + " REAL NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");
    }

    /**
     * Check if a user exists by username or email.
     */
    public boolean isUserExists(SQLiteDatabase db, String username, String email) {
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + " = ? OR " + COLUMN_EMAIL + " = ?",
                new String[]{username, email},
                null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DatabaseError", "Failed to check user existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a new user in the database.
     */
    public long createUser(SQLiteDatabase db, String username, String email, String hashedPassword) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedPassword);

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseError", "Failed to create user: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get the username for a given user ID.
     */
    public String getUsername(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Failed to retrieve username: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}