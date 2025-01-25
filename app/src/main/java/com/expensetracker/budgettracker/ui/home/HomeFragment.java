package com.expensetracker.budgettracker.ui.home;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.expensetracker.budgettracker.LoginActivity;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.adapters.FlashcardsAdapter;
import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.databinding.FragmentHomeBinding;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.models.Transaction;
import com.expensetracker.budgettracker.ui.dashboard.TransactionViewModel;
import com.expensetracker.budgettracker.utils.DatePickerHelper;
import com.expensetracker.budgettracker.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private TransactionViewModel transactionViewModel;
    private FlashcardsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        HomeViewModelFactory factory = new HomeViewModelFactory(
                (Application) requireContext().getApplicationContext(),
                transactionViewModel
        );

        homeViewModel = new ViewModelProvider(requireActivity(), factory).get(HomeViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupUserMenu();
    }

    private void setupRecyclerView() {
        adapter = new FlashcardsAdapter(requireContext(), new ArrayList<>(), this::showInputDialog);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
    }

    // In HomeFragment.java, ensure LiveData observers are active
    private void setupObservers() {
        homeViewModel.getFlashcards().observe(getViewLifecycleOwner(), flashcards -> {
            if (flashcards != null) {
                adapter.updateFlashcards(flashcards);
                updateSummaryText(); // Force UI update
            }
        });

        transactionViewModel.getBalance().observe(getViewLifecycleOwner(), balance -> updateSummaryText());
        transactionViewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> updateSummaryText());
        transactionViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> updateSummaryText());
    }

    @SuppressLint("DefaultLocale")
    private void updateSummaryText() {
        double income = transactionViewModel.getTotalIncome().getValue() != null ?
                transactionViewModel.getTotalIncome().getValue() : 0.0;
        double expense = transactionViewModel.getTotalExpense().getValue() != null ?
                transactionViewModel.getTotalExpense().getValue() : 0.0;
        double balance = income - expense;

        binding.summaryText.setText(String.format("Income: %s | Expense: %s | Balance: %s",
                HomeViewModel.formatCurrency(income),
                HomeViewModel.formatCurrency(expense),
                HomeViewModel.formatCurrency(balance)));
    }


    private void showInputDialog(Flashcard flashcard, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_transaction, null);

        TextInputLayout amountLayout = dialogView.findViewById(R.id.input_amount_layout);
        TextInputEditText inputAmount = dialogView.findViewById(R.id.input_amount);
        TextInputLayout dateLayout = dialogView.findViewById(R.id.input_date_layout);
        TextInputEditText inputDate = dialogView.findViewById(R.id.input_date);

        // Date picker setup
        inputDate.setOnClickListener(v ->
                DatePickerHelper.showDatePicker(requireContext(), inputDate)
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_transaction_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, null) // Set to null to override default dismissal
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // Clear previous errors
                amountLayout.setError(null);
                dateLayout.setError(null);

                String amountStr = Objects.requireNonNull(inputAmount.getText()).toString().trim();
                String date = Objects.requireNonNull(inputDate.getText()).toString().trim();

                // Validate amount
                if (amountStr.isEmpty()) {
                    amountLayout.setError(getString(R.string.amount_required));
                    return;
                }

                // Validate date
                if (date.isEmpty()) {
                    dateLayout.setError(getString(R.string.date_required));
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        amountLayout.setError(getString(R.string.amount_positive));
                        return;
                    }

                    // Determine transaction type using string resources
                    String type = flashcard.getLabel().equalsIgnoreCase(getString(R.string.category_salary))
                            ? "income"
                            : "expense";

                    Transaction transaction = new Transaction(
                            flashcard.getLabel(),
                            amount,
                            date,
                            type
                    );

                    transactionViewModel.addTransaction(transaction);
                    Toast.makeText(requireContext(), R.string.transaction_added, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    amountLayout.setError(getString(R.string.invalid_amount_format));
                }
            });
        });

        dialog.show();
    }
    private void setupUserMenu() {
        SessionManager session = new SessionManager(requireContext());
        String username = null;
        try (DatabaseHelper dbHelper = new DatabaseHelper(requireContext())) {
            username = dbHelper.getUsername(session.getUserId());
        } catch (Exception e) {
            Log.e("HomeFragment", "Error accessing database", e);
        }

        TextView tvUsername = binding.toolbar.findViewById(R.id.tv_username);
        tvUsername.setText(username != null ? username : "User");

        ImageButton btnMenu = binding.toolbar.findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> showUserMenu());

        if (username != null) {
            tvUsername.setText(username);
        } else {
            Log.e("HomeFragment", "Username TextView not found or username is null");
        }
    }
    private void showUserMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), binding.toolbar.findViewById(R.id.btn_menu));
        popup.getMenu().add("Log Out");
        popup.setOnMenuItemClickListener(item -> {
            new SessionManager(requireContext()).logoutUser();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return true;
        });
        popup.show();
    }
    private boolean validateInput(String amountStr, String date) {
        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(amountStr);
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount format!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}