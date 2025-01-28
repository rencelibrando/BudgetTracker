package com.expensetracker.budgettracker.ui.dashboard;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> totalIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalExpense = new MutableLiveData<>(0.0);
    private final MediatorLiveData<Double> balance = new MediatorLiveData<>();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        executorService = Executors.newSingleThreadExecutor();

        balance.addSource(totalIncome, income -> balance.setValue(income - getNonNullValue(totalExpense.getValue())));
        balance.addSource(totalExpense, expense -> balance.setValue(getNonNullValue(totalIncome.getValue()) - expense));

        loadTransactions();
    }

    private double getNonNullValue(Double value) {
        return value != null ? value : 0.0;
    }

    public void loadTransactions() {
        executorService.execute(() -> {
            List<Transaction> transactionsList = new ArrayList<>();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            try (var cursor = db.query(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    new String[]{
                            DatabaseHelper.COLUMN_TRANSACTION_ID,
                            DatabaseHelper.COLUMN_AMOUNT,
                            DatabaseHelper.COLUMN_CATEGORY,
                            DatabaseHelper.COLUMN_DATE,
                            DatabaseHelper.COLUMN_TYPE
                    },
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(new SessionManager(getApplication()).getUserId())},
                    null, null, DatabaseHelper.COLUMN_DATE + " DESC"
            )) {

                while (cursor.moveToNext()) {
                    Transaction transaction = new Transaction(
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE))
                    );
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ID)));
                    transactionsList.add(transaction);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading transactions", e);
            }

            transactions.postValue(Collections.unmodifiableList(transactionsList));
            updateTotals(transactionsList);
        });
    }

    public void addTransaction(Transaction transaction) {
        executorService.execute(() -> {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_AMOUNT, transaction.getAmount());
                values.put(DatabaseHelper.COLUMN_CATEGORY, transaction.getCategory());
                values.put(DatabaseHelper.COLUMN_DATE, transaction.getDate());
                values.put(DatabaseHelper.COLUMN_TYPE, transaction.getType());
                values.put(DatabaseHelper.COLUMN_USER_ID, new SessionManager(getApplication()).getUserId());

                db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
                loadTransactions();
            } catch (Exception e) {
                Log.e(TAG, "Error adding transaction", e);
            }
        });
    }

    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                db.delete(DatabaseHelper.TABLE_TRANSACTIONS,
                        DatabaseHelper.COLUMN_TRANSACTION_ID + " = ?",
                        new String[]{String.valueOf(transaction.getId())});
                loadTransactions();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting transaction", e);
            }
        });
    }

    private void updateTotals(List<Transaction> transactionsList) {
        double income = 0.0;
        double expense = 0.0;

        for (Transaction t : transactionsList) {
            if ("income".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        totalIncome.postValue(income);
        totalExpense.postValue(expense);
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Double> getBalance() {
        return balance;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}