package com.expensetracker.budgettracker.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final TransactionViewModel transactionViewModel;

    public HomeViewModelFactory(Application application, TransactionViewModel transactionViewModel) {
        this.application = application;
        this.transactionViewModel = transactionViewModel;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(application, transactionViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}