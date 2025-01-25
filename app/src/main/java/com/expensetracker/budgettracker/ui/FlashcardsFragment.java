package com.expensetracker.budgettracker.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.adapters.FlashcardsAdapter;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.ui.home.HomeViewModel;

import java.util.ArrayList;

public class FlashcardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FlashcardsAdapter adapter;
    private HomeViewModel homeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcards, container, false);

        recyclerView = view.findViewById(R.id.flashcards_recycler_view);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupRecyclerView();
        observeFlashcards();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FlashcardsAdapter(requireContext(), new ArrayList<>(), this::showInputDialog);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void observeFlashcards() {
        homeViewModel.getFlashcards().observe(getViewLifecycleOwner(), flashcardsList -> {
            if (flashcardsList != null) {
                adapter.updateFlashcards(flashcardsList);
            }
        });
    }

    private void showInputDialog(Flashcard flashcard, int position) {
        Context context = requireContext();
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(getString(R.string.enter_amount_hint));

        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.add_expense_title))
                .setMessage(getString(R.string.add_expense_message, flashcard.getLabel()))
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String amountStr = input.getText().toString().trim();
                    if (validateAmount(amountStr)) {
                        updateFlashcardAmount(flashcard, position, Double.parseDouble(amountStr));
                    } else {
                        Toast.makeText(context, R.string.invalid_amount_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean validateAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void updateFlashcardAmount(Flashcard flashcard, int position, double amount) {
        String formattedAmount = HomeViewModel.formatCurrency(amount);

        flashcard.setAmount(formattedAmount);
        adapter.updateFlashcard(position, formattedAmount);

        homeViewModel.updateFlashcardAmount(flashcard.getLabel(), amount);
    }
}