package com.expensetracker.budgettracker.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.models.Transaction;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<Flashcard>> flashcards = new MutableLiveData<>(new ArrayList<>());
    private final TransactionViewModel transactionViewModel;
    private final Observer<List<Transaction>> transactionsObserver = this::updateFlashcardsFromTransactions;

    public HomeViewModel(TransactionViewModel transactionViewModel) {
        this.transactionViewModel = transactionViewModel;
        initializeDefaultFlashcards();
        transactionViewModel.getTransactions().observeForever(transactionsObserver);
        transactionViewModel.loadTransactions();
    }

    public static String formatCurrency(double amount) {
        try {
            return String.format(Locale.getDefault(), "₱%.2f", amount);
        } catch (Exception e) {
            return "₱0.00";
        }
    }

    private void updateFlashcardsFromTransactions(List<Transaction> transactions) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            List<Flashcard> currentFlashcards = flashcards.getValue();
            if (currentFlashcards == null) return;

            // Create a new list of Flashcard instances
            List<Flashcard> updatedFlashcards = new ArrayList<>();
            for (Flashcard original : currentFlashcards) {
                double total = transactions.stream()
                        .filter(t -> t.getCategory().equalsIgnoreCase(original.getLabel())) // Case-insensitive match
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                // Create a NEW Flashcard instance
                updatedFlashcards.add(new Flashcard(
                        original.getIconResId(),
                        original.getLabel(),
                        formatCurrency(total)
                ));
            }

            flashcards.postValue(updatedFlashcards); // Post the new list
        });
    }

    private void initializeDefaultFlashcards() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Flashcard> defaultFlashcards = new ArrayList<>();
            defaultFlashcards.add(new Flashcard(R.drawable.ic_food, "Food & Drink", formatCurrency(0.0)));
            defaultFlashcards.add(new Flashcard(R.drawable.ic_transport, "Transportation", formatCurrency(0.0)));
            defaultFlashcards.add(new Flashcard(R.drawable.ic_housing, "Housing & Utilities", formatCurrency(0.0)));
            defaultFlashcards.add(new Flashcard(R.drawable.ic_personal_care, "Personal Care", formatCurrency(0.0)));
            defaultFlashcards.add(new Flashcard(R.drawable.ic_shopping, "Shopping", formatCurrency(0.0)));
            defaultFlashcards.add(new Flashcard(R.drawable.ic_salary, "Salary", formatCurrency(0.0)));
            flashcards.postValue(defaultFlashcards);
        });
    }

    public void updateFlashcardAmount(Flashcard flashcard, double newAmount) {
        List<Flashcard> current = flashcards.getValue();
        if (current == null) return;

        // Create a new list with updated Flashcard
        List<Flashcard> newList = new ArrayList<>();
        for (Flashcard f : current) {
            if (f.getLabel().equals(flashcard.getLabel())) {
                newList.add(new Flashcard(
                        f.getIconResId(),
                        f.getLabel(),
                        formatCurrency(newAmount) // New instance
                ));
            } else {
                newList.add(f);
            }
        }
        flashcards.postValue(newList); // Update LiveData
    }

    public LiveData<List<Flashcard>> getFlashcards() {
        return flashcards;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        transactionViewModel.getTransactions().removeObserver(transactionsObserver);
    }
}
