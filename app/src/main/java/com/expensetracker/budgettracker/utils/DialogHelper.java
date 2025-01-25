package com.expensetracker.budgettracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Flashcard;
import com.expensetracker.budgettracker.adapters.FlashcardsAdapter;
import com.expensetracker.budgettracker.ui.home.HomeViewModel;

public class DialogHelper {

    public interface DialogCallback {
        void onAmountUpdated(String newAmount);
    }

    public static void showEditAmountDialog(@NonNull Context context, @NonNull Flashcard flashcard, @NonNull DialogCallback callback) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(context.getString(R.string.enter_amount_hint));
        input.setText(flashcard.getAmount().replace("₱", ""));

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.add_expense_title))
                .setMessage(context.getString(R.string.add_expense_message, flashcard.getLabel()))
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String amountStr = input.getText().toString().trim();
                    if (validateAmount(amountStr)) {
                        String formattedAmount = "₱" + amountStr;
                        callback.onAmountUpdated(formattedAmount);
                    } else {
                        Toast.makeText(context, R.string.invalid_amount_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static boolean validateAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Corrected integration example
    public static void integrateExample(Context context, Flashcard flashcard, FlashcardsAdapter adapter, HomeViewModel homeViewModel, int position) {
        DialogHelper.showEditAmountDialog(
                context,
                flashcard,
                newAmount -> {
                    String numericValue = newAmount.replace("₱", "");
                    flashcard.setAmount(newAmount);
                    // Pass the updated String amount to the adapter
                    adapter.updateFlashcard(position, newAmount);
                    // Pass the Flashcard's ID (or relevant String) and amount to ViewModel
                    homeViewModel.updateFlashcardAmount(flashcard.getId(), Double.parseDouble(numericValue));
                }
        );
    }
}