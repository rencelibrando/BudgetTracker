// 1. Add BudgetAdapter class (new file)
package com.expensetracker.budgettracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.BudgetItem;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private final Context context;
    private List<BudgetItem> budgetItems;

    public BudgetAdapter(Context context, List<BudgetItem> budgetItems) {
        this.context = context;
        this.budgetItems = budgetItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.budget_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BudgetItem item = budgetItems.get(position);
        holder.categoryText.setText(item.getCategory());
        holder.amountText.setText(context.getString(R.string.currency_format, item.getAmount()));
    }

    @Override
    public int getItemCount() {
        return budgetItems.size();
    }

    public void updateBudgets(List<BudgetItem> newItems) {
        budgetItems = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;
        TextView amountText;

        ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.tv_category);
            amountText = itemView.findViewById(R.id.tv_amount);
        }
    }
}