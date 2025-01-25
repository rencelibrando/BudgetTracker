package com.expensetracker.budgettracker.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.adapters.TransactionsAdapter;
import com.expensetracker.budgettracker.databinding.FragmentTransactionBinding;
import com.expensetracker.budgettracker.models.Transaction;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionFragment extends Fragment {

    private static final String TAG = "TransactionFragment";
    private TransactionsAdapter transactionsAdapter;
    private TransactionViewModel transactionViewModel;
    private FragmentTransactionBinding binding;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupRecyclerView();

        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                transactionsAdapter.updateTransactions(transactions);
            }
        });
        transactionViewModel.getTotalIncome().observe(getViewLifecycleOwner(), income ->
                binding.totalIncome.setText(getString(R.string.total_income, income)));

        transactionViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense ->
                binding.totalExpenses.setText(getString(R.string.total_expenses, expense)));
    }

    private void setupRecyclerView() {
        transactionsAdapter = new TransactionsAdapter(new ArrayList<>(), new TransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction, int position) {
                Log.d(TAG, "Transaction clicked: " + transaction);
            }
            @Override
            public void onItemDelete(Transaction transaction, int position) {
                executorService.execute(() -> {
                    Log.d(TAG, "Transaction deleted: " + transaction);
                    transactionViewModel.deleteTransaction(transaction);
                });
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(transactionsAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}