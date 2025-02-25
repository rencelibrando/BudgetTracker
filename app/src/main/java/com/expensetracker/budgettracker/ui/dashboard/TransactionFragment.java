package com.expensetracker.budgettracker.ui.dashboard;
import java.util.ArrayList;
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
import java.util.Locale;
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
        // Inflate the layout using ViewBinding
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel and RecyclerView
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        setupRecyclerView();
        setupObservers();

        // Setup swipe-to-refresh listener
        binding.swipeRefresh.setOnRefreshListener(() -> {
            transactionViewModel.loadTransactions();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupRecyclerView() {
        // Initialize adapter with click listeners
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

        // Configure RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(transactionsAdapter);
    }

    private void setupObservers() {
        // Observe transactions list and update adapter
        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactionsAdapter::updateTransactions);

        // Observe total income and update UI
        transactionViewModel.getTotalIncome().observe(getViewLifecycleOwner(), income ->
                binding.totalIncome.setText(getString(R.string.total_income_label,
                        String.format(Locale.getDefault(), "₱%.2f", income))));

        // Observe total expenses and update UI
        transactionViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense ->
                binding.totalExpenses.setText(getString(R.string.total_expenses_label,
                        String.format(Locale.getDefault(), "₱%.2f", expense))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear binding to avoid memory leaks
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service to release resources
        executorService.shutdown();
    }
}