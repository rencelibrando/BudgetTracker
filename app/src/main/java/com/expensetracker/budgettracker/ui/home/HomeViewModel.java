package com.expensetracker.budgettracker.ui.home;
import androidx.lifecycle.Observer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    // Change initialization to avoid null
    private MutableLiveData<List<Flashcard>> flashcards = new MutableLiveData<>(new ArrayList<>());

    public TransactionViewModel transactionViewModel;
    private final Observer<List<Transaction>> transactionsObserver =
            this::updateFlashcardsFromTransactions;

    public HomeViewModel(TransactionViewModel transactionViewModel) {
        this.transactionViewModel = transactionViewModel;
        flashcards = new MutableLiveData<>();
        initializeDefaultFlashcards();

        transactionViewModel.getTransactions().observeForever(transactionsObserver);
    }


    public static String formatCurrency(double amount) {
        try {
            return String.format(Locale.getDefault(), "₱%.2f", amount);
        } catch (Exception e) {
            return "₱0.00";
        }
    }

    public void updateFlashcardsFromTransactions(List<Transaction> transactions) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            List<Flashcard> currentFlashcards = flashcards.getValue();
            if (currentFlashcards == null) return;

            // Reuse existing flashcards instead of creating new ones
            for (Flashcard flashcard : currentFlashcards) {
                double total = transactions.stream()
                        .filter(t -> t.getCategory().equals(flashcard.getLabel()))
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                flashcard.setAmount(formatCurrency(total)); // Update amount of existing instance
            }

            flashcards.postValue(currentFlashcards);
        });
    }
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private void initializeDefaultFlashcards() {
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

        for (Flashcard f : current) {
            if (f.getLabel().equalsIgnoreCase(flashcard.getLabel())) {
                f.setAmount(formatCurrency(newAmount));
                break;
            }
        }
        flashcards.postValue(current);
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