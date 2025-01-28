package com.expensetracker.budgettracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private final List<Transaction> transactions;
    private final OnItemClickListener listener;
    private final Map<String, Integer> categoryIcons = new HashMap<String, Integer>() {{
        put("food & drink", R.drawable.ic_food);
        put("transportation", R.drawable.ic_transport);
        put("salary", R.drawable.ic_salary);
        put("housing", R.drawable.ic_housing);
        put("shopping", R.drawable.ic_shopping);
    }};

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction, int position);
        void onItemDelete(Transaction transaction, int position);
    }

    public TransactionsAdapter(List<Transaction> transactions, OnItemClickListener listener) {
        this.transactions = new ArrayList<>(transactions);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Context context = holder.itemView.getContext();

        try {
            // Set category icon
            String category = transaction.getCategory() != null ?
                    transaction.getCategory().toLowerCase() : "other";
            Integer iconRes = categoryIcons.getOrDefault(category, R.drawable.ic_category_default);
            holder.categoryIcon.setImageResource(iconRes != null ? iconRes : R.drawable.ic_category_default);

            // Set category name
            holder.category.setText(transaction.getCategory());

            // Set amount color and format
            String type = transaction.getType() != null ?
                    transaction.getType().toLowerCase() : "expense";
            int colorRes = type.equals("income") ? R.color.green_500 : R.color.red_500;
            holder.amount.setTextColor(ContextCompat.getColor(context, colorRes));

            // Format amount with currency
            String amountPrefix = type.equals("income") ? "+" : "-";
            holder.amount.setText(String.format(Locale.getDefault(),
                    "%s₱%.2f", amountPrefix, transaction.getAmount()));

            // Format date
            holder.date.setText(formatDate(transaction.getDate()));

        } catch (Exception e) {
            Log.e("Adapter", "Error binding transaction", e);
            holder.categoryIcon.setImageResource(R.drawable.ic_category_default);
            holder.amount.setText(context.getString(R.string.default_amount));
            holder.date.setText(context.getString(R.string.date_not_available));
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(transactions.get(position), position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemDelete(transactions.get(position), position);
            }
            return true;
        });
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(rawDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("DateFormat", "Error formatting date: " + rawDate, e);
            return "Invalid Date";
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new TransactionDiffCallback(transactions, newTransactions)
        );
        transactions.clear();
        transactions.addAll(newTransactions);
        diffResult.dispatchUpdatesTo(this);
    }

    static class TransactionDiffCallback extends DiffUtil.Callback {
        private final List<Transaction> oldList;
        private final List<Transaction> newList;

        TransactionDiffCallback(List<Transaction> oldList, List<Transaction> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getDate().equals(newList.get(newPos).getDate());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).equals(newList.get(newPos));
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final ImageView categoryIcon;
        final TextView category;
        final TextView amount;
        final TextView date;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            category = itemView.findViewById(R.id.transaction_category);
            amount = itemView.findViewById(R.id.transaction_amount);
            date = itemView.findViewById(R.id.transaction_date);
        }
    }
}
