package com.expensetracker.budgettracker.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.models.Transaction;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;
import com.expensetracker.budgettracker.utils.SessionManager;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Flashcard>> flashcards = new MutableLiveData<>();
    private final TransactionViewModel transactionViewModel;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final long userId;
    public HomeViewModel(Application application, TransactionViewModel transactionViewModel) {
        super(application);
        this.transactionViewModel = transactionViewModel;
        this.databaseHelper = new DatabaseHelper(application);
        this.userId = new SessionManager(application).getUserId();
        initializeFlashcards();
        transactionViewModel.getTransactions().observeForever(this::updateFlashcardsFromTransactions);
    }

    private void initializeFlashcards() {
        executor.execute(() -> {
            // Load from database or insert defaults
            if (databaseHelper.isUserFlashcardsEmpty(userId)) {
                insertDefaultFlashcards();
            }
            refreshFlashcardsFromDb();
        });
    }

    private void insertDefaultFlashcards() {
        int[] icons = {
                R.drawable.ic_food,
                R.drawable.ic_transport,
                R.drawable.ic_housing,
                R.drawable.ic_personal_care,
                R.drawable.ic_shopping,
                R.drawable.ic_salary
        };

        String[] labels = {
                "Food & Drink",
                "Transportation",
                "Housing & Utilities",
                "Personal Care",
                "Shopping",
                "Salary"
        };

        for (int i = 0; i < labels.length; i++) {
            databaseHelper.createFlashcard(userId, labels[i], icons[i], 0.0); // Pass userId
        }
    }

    private void updateFlashcardsFromTransactions(List<Transaction> transactions) {
        executor.execute(() -> {
            List<Flashcard> dbFlashcards = databaseHelper.getAllFlashcards(userId);
            for (Flashcard flashcard : dbFlashcards) {
                double total = calculateTotalForCategory(transactions, flashcard.getLabel());
                databaseHelper.updateFlashcardAmount(userId, flashcard.getLabel(), total);
            }
            refreshFlashcardsFromDb();
        });
    }
    private double calculateTotalForCategory(List<Transaction> transactions, String category) {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        return transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(t -> "income".equalsIgnoreCase(t.getType()) ? t.getAmount() : -t.getAmount())
                .sum();
    }

    private void refreshFlashcardsFromDb() {
        List<Flashcard> updated = databaseHelper.getAllFlashcardsWithFormattedAmount(userId); // Pass userId
        flashcards.postValue(updated);
    }
    public void refreshFlashcards() {
        executor.execute(this::refreshFlashcardsFromDb);
    }
    public void updateFlashcardAmount(String label, double newAmount) {
        executor.execute(() -> {
            databaseHelper.updateFlashcardAmount(userId, label, newAmount); // Pass userId
            refreshFlashcardsFromDb();
        });
    }

    public LiveData<List<Flashcard>> getFlashcards() {
        return flashcards;
    }

    public static String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "₱%.2f", amount);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
        transactionViewModel.getTransactions().removeObserver(this::updateFlashcardsFromTransactions);
    }
}