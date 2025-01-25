package com.expensetracker.budgettracker.ui.home;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

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

import java.util.ArrayList;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private TransactionViewModel transactionViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUserMenu();
        
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        HomeViewModelFactory factory = new HomeViewModelFactory(transactionViewModel);
        homeViewModel = new ViewModelProvider(requireActivity(), factory).get(HomeViewModel.class);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        FlashcardsAdapter adapter = new FlashcardsAdapter(
                requireContext(), // Add Context as first parameter
                new ArrayList<>(), // Empty list of Flashcard (type inferred correctly)
                this::showInputDialog  // Listener
        );
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
    }

    @SuppressLint("DefaultLocale")
    private void setupObservers() {
        homeViewModel.getFlashcards().observe(getViewLifecycleOwner(), flashcards -> {
            if (flashcards != null && binding.recyclerView.getAdapter() != null) {
                ((FlashcardsAdapter) binding.recyclerView.getAdapter()).updateFlashcards(flashcards);
            }
        });
        transactionViewModel.getBalance().observe(getViewLifecycleOwner(), balance -> updateSummaryText());
        transactionViewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> updateSummaryText());
        transactionViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> updateSummaryText());
    }

    private void updateSummaryText() {
        double income = transactionViewModel.getTotalIncome().getValue() != null ?
                transactionViewModel.getTotalIncome().getValue() : 0.0;
        double expense = transactionViewModel.getTotalExpense().getValue() != null ?
                transactionViewModel.getTotalExpense().getValue() : 0.0;
        double balance = transactionViewModel.getBalance().getValue() != null ?
                transactionViewModel.getBalance().getValue() : 0.0;

        binding.summaryText.setText(String.format("Income: ₱%.2f | Expense: ₱%.2f | Balance: ₱%.2f",
                income, expense, balance));
    }


    private void showInputDialog(Flashcard flashcard, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_transaction, null);

        TextInputEditText inputAmount = dialogView.findViewById(R.id.input_amount);
        TextInputEditText inputDate = dialogView.findViewById(R.id.input_date);

        inputDate.setOnClickListener(v ->
                DatePickerHelper.showDatePicker(requireContext(), inputDate)
        );

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_transaction_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String amountStr = Objects.requireNonNull(inputAmount.getText()).toString().trim();
                    String date = Objects.requireNonNull(inputDate.getText()).toString().trim();

                    if (validateInput(amountStr, date)) {
                        double amount = Double.parseDouble(amountStr);
                        String type = flashcard.getLabel().equalsIgnoreCase("salary")
                                ? "income"
                                : "expense";

                        Transaction transaction = new Transaction(
                                flashcard.getLabel(),
                                amount,
                                date,
                                type
                        );
                        transactionViewModel.addTransaction(transaction);

                        homeViewModel.updateFlashcardAmount(
                                flashcard,
                                amount
                        );
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    private void setupUserMenu() {
        SessionManager session = new SessionManager(requireContext());
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        String username = dbHelper.getUsername(session.getUserId());

        TextView tvUsername = binding.toolbar.findViewById(R.id.tv_username);
        tvUsername.setText(username != null ? username : "User");

        ImageButton btnMenu = binding.toolbar.findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> showUserMenu());

        if (tvUsername != null && username != null) {
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