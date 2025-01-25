
package com.expensetracker.budgettracker.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final TransactionViewModel transactionViewModel;

    public HomeViewModelFactory(TransactionViewModel transactionViewModel) {
        this.transactionViewModel = transactionViewModel;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(transactionViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}