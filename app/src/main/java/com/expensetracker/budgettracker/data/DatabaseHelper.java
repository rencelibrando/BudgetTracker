package com.expensetracker.budgettracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.models.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BudgetTracker.db";
    private static final int DATABASE_VERSION = 4;
    private final Context context;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_FLASHCARDS = "flashcards";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LABEL = "label";

    // Users table columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Budgets table columns
    public static final String COLUMN_BUDGET_AMOUNT = "budget_amount";

    // Flashcards table columns
    public static final String COLUMN_ICON_RES_ID = "icon_res_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            createUsersTable(db);
            createTransactionsTable(db);
            createBudgetsTable(db);
            createFlashcardsTable(db);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("Database", "Error creating database", e);
        } finally {
            db.endTransaction();
        }
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    private void createTransactionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_AMOUNT + " REAL NOT NULL, "
                + COLUMN_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + "type TEXT NOT NULL," // Add this line
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE)");
    }

    private void createBudgetsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_CATEGORY + " TEXT UNIQUE NOT NULL, "
                + COLUMN_BUDGET_AMOUNT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE)");
    }
    public void createFlashcard(long userId, String label, int iconResId, double amount) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_ICON_RES_ID, iconResId);
        values.put(COLUMN_AMOUNT, amount);
        getWritableDatabase().insert(TABLE_FLASHCARDS, null, values);
    }
    private void createFlashcardsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FLASHCARDS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL, " // Added user_id
                + COLUMN_LABEL + " TEXT NOT NULL, "
                + COLUMN_AMOUNT + " REAL DEFAULT 0, "
                + COLUMN_ICON_RES_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();
            if (oldVersion < 4) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
                createTransactionsTable(db);
            }

            if (oldVersion < 3) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
                createFlashcardsTable(db);
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("Database", "Error upgrading database", e);
            recreateDatabase(db);
        } finally {
            db.endTransaction();
        }
    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    // In DatabaseHelper.java
    public List<Flashcard> getAllFlashcards(long userId) {
        List<Flashcard> flashcards = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(TABLE_FLASHCARDS,
                new String[]{COLUMN_LABEL, COLUMN_AMOUNT, COLUMN_ICON_RES_ID},
                COLUMN_USER_ID + " = ?", // Filter by user
                new String[]{String.valueOf(userId)}, // Use parameter
                null, null, null)) {

            while (cursor.moveToNext()) {
                String label = cursor.getString(0);
                double amount = cursor.getDouble(1);
                int iconResId = cursor.getInt(2);

                // Convert double to String before passing to Flashcard
                flashcards.add(new Flashcard(iconResId, label, String.valueOf(amount)));
            }
        }
        return flashcards;
    }
    private void recreateDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
        onCreate(db);
    }

    private void insertDefaultFlashcards(SQLiteDatabase db, long userId) {
        db.beginTransaction();
        try {
            int[][] defaultFlashcards = {
                    {R.string.category_food, R.drawable.ic_food},
                    {R.string.category_transport, R.drawable.ic_transport},
                    {R.string.category_housing, R.drawable.ic_housing},
                    {R.string.category_personal_care, R.drawable.ic_personal_care},
                    {R.string.category_shopping, R.drawable.ic_shopping},
                    {R.string.category_salary, R.drawable.ic_salary}
            };

            ContentValues values = new ContentValues();
            for (int[] flashcard : defaultFlashcards) {
                values.clear(); // Clear previous values
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_LABEL, context.getString(flashcard[0]));
                values.put(COLUMN_ICON_RES_ID, flashcard[1]);
                values.put(COLUMN_AMOUNT, 0.0); // Add default amount

                db.insertWithOnConflict(TABLE_FLASHCARDS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Database", "Error inserting default flashcards", e);
        } finally {
            db.endTransaction();
        }
    }

    // Existing methods with improved error handling

    public void updateFlashcardAmount(long userId, String label, double amount) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_AMOUNT, amount);
            db.update(TABLE_FLASHCARDS, values,
                    COLUMN_LABEL + " = ? AND " + COLUMN_USER_ID + " = ?",
                    new String[]{label, String.valueOf(userId)});
            Log.d("DatabaseHelper", "Updated " + label + " to " + amount);
        } catch (SQLException e) {
            Log.e("Database", "Update failed", e);
        }
    }

    public List<Flashcard> getAllFlashcardsWithFormattedAmount(long userId) {
        List<Flashcard> flashcards = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(TABLE_FLASHCARDS,
                new String[]{COLUMN_LABEL, COLUMN_AMOUNT, COLUMN_ICON_RES_ID},
                COLUMN_USER_ID + " = ?", // Filter by user
                new String[]{String.valueOf(userId)},
                null, null, null)) {

            while (cursor.moveToNext()) {
                String label = cursor.getString(0);
                double amount = cursor.getDouble(1);
                int iconResId = cursor.getInt(2);
                flashcards.add(new Flashcard(iconResId, label, formatCurrency(amount)));
            }
        } catch (SQLException e) {
            Log.e("Database", "Error retrieving flashcards", e);
        }
        return flashcards;
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "₱%.2f", amount);
    }

    public boolean isUserFlashcardsEmpty(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_FLASHCARDS +
                        " WHERE " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)})) {
            cursor.moveToFirst();
            return cursor.getInt(0) == 0;
        }
    }

    // Existing user management methods with transactions

    public long createUser(String username, String email, String hashedPassword) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, hashedPassword);

            long userId = db.insertWithOnConflict(TABLE_USERS, null, values,
                    SQLiteDatabase.CONFLICT_ABORT);

            db.setTransactionSuccessful();
            return userId;
        } catch (SQLException e) {
            Log.e("Database", "Error creating user", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }
    public List<Transaction> getAllTransactions(long userId) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(TABLE_TRANSACTIONS,
                null, // All columns
                selection,
                selectionArgs,
                null, null, null)) {

            while (cursor.moveToNext()) {
                Transaction transaction = new Transaction(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")) // Add type column if missing
                );
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    public String getUsername(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null)) {

            return cursor.moveToFirst() ? cursor.getString(0) : null;
        } catch (SQLException e) {
            Log.e("Database", "Error getting username", e);
            return null;
        }
    }
}