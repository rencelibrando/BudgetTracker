package com.expensetracker.budgettracker.ui.dashboard;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.models.Transaction;
import com.expensetracker.budgettracker.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {
    private static final String TAG = "TransactionViewModel";
    private final Observer<List<Transaction>> transactionsObserver =
            transactions -> updateTotals();
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executorService;

    // Database schema constants (must match DatabaseHelper.java)
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COL_ID = "id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DATE = "date";
    private static final String COL_TYPE = "type";
    private static final String COLUMN_USER_ID = "user_id"; // Must match your database column name

    // LiveData containers
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> totalIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalExpense = new MutableLiveData<>(0.0);
    private final MediatorLiveData<Double> balance = new MediatorLiveData<>();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        executorService = Executors.newSingleThreadExecutor();
        transactions.observeForever(transactions -> updateTotals());
        // Configure balance calculations
        balance.addSource(totalIncome, income ->
                balance.setValue(income - (totalExpense.getValue() != null ? totalExpense.getValue() : 0.0))
        );
        balance.addSource(totalExpense, expense ->
                balance.setValue((totalIncome.getValue() != null ? totalIncome.getValue() : 0.0) - expense)
        );

        loadTransactions();
    }

    public void loadTransactions() {
        executorService.execute(() -> {
            List<Transaction> transactionsList = new ArrayList<>();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            // Add user-specific filtering
            String selection = COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(new SessionManager(getApplication()).getUserId())};

            try (Cursor cursor = db.query(
                    TABLE_TRANSACTIONS,
                    new String[]{COL_ID, COL_AMOUNT, COL_CATEGORY, COL_DATE, COL_TYPE},
                    selection,  // Updated: Apply user filter
                    selectionArgs,  // Updated: User ID argument
                    null,
                    null,
                    COL_DATE + " DESC")) {

                while (cursor.moveToNext()) {
                    Transaction transaction = new Transaction(
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))
                    );
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                    transactionsList.add(transaction);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading transactions", e);
            }

            transactions.postValue(Collections.unmodifiableList(transactionsList));
        });
    }

    public void addTransaction(Transaction transaction) {
        executorService.execute(() -> {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(COL_AMOUNT, transaction.getAmount());
                values.put(COL_CATEGORY, transaction.getCategory());
                values.put(COL_DATE, transaction.getDate());
                values.put(COL_TYPE, transaction.getType());
                values.put(COLUMN_USER_ID, new SessionManager(getApplication()).getUserId());

                db.insert(TABLE_TRANSACTIONS, null, values);
                loadTransactions(); // Refresh data
            } catch (Exception e) {
                Log.e(TAG, "Error adding transaction", e);
            }
        });
    }

    // LiveData getters
    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }
    public LiveData<Double> getBalance() {
        return balance;
    }
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    private void updateTotals() {
        double income = 0;
        double expense = 0;

        List<Transaction> currentList = transactions.getValue();
        if (currentList != null && !currentList.isEmpty()) {
            for (Transaction t : currentList) {
                if ("income".equalsIgnoreCase(t.getType())) {
                    income += t.getAmount();
                } else {
                    expense += t.getAmount();
                }
            }
        }

        totalIncome.postValue(income);
        totalExpense.postValue(expense);
    }
    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                db.delete(TABLE_TRANSACTIONS,
                        COL_ID + "=?",
                        new String[]{String.valueOf(transaction.getId())});
                loadTransactions();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting transaction", e);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        transactions.removeObserver(transactionsObserver); // Remove observer to prevent leaks
        executorService.shutdown();
    }
}