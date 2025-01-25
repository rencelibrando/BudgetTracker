package com.expensetracker.budgettracker.ui.notifications;

import static com.expensetracker.budgettracker.data.DatabaseHelper.COLUMN_BUDGET_AMOUNT;
import static com.expensetracker.budgettracker.data.DatabaseHelper.COLUMN_CATEGORY;
import static com.expensetracker.budgettracker.data.DatabaseHelper.TABLE_BUDGETS;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.adapters.BudgetAdapter;
import com.expensetracker.budgettracker.data.DatabaseHelper;
import com.expensetracker.budgettracker.databinding.FragmentBudgetBinding;
import com.expensetracker.budgettracker.models.BudgetItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private DatabaseHelper databaseHelper;
    private BudgetAdapter adapter;
    private final List<BudgetItem> budgetItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        databaseHelper = new DatabaseHelper(requireContext());

        setupToolbar();
        setupBudgetRecyclerView();
        loadBudgets();

        return binding.getRoot();
    }

    private void setupBudgetRecyclerView() {
        adapter = new BudgetAdapter(requireContext(), budgetItems);
        binding.budgetRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.budgetRecyclerView.setAdapter(adapter);
    }

    private void loadBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<BudgetItem> budgetList = new ArrayList<>();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            try (Cursor cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_CATEGORY, COLUMN_BUDGET_AMOUNT},
                    null, null, null, null, null)) {

                while (cursor.moveToNext()) {
                    BudgetItem item = new BudgetItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_AMOUNT))
                    );
                    budgetList.add(item);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.updateBudgets(budgetList);
                        setupPieChart(budgetList);
                    });
                }
            }
        });
    }

    private void setupPieChart(List<BudgetItem> items) {
        PieChart pieChart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();

        for (BudgetItem item : items) {
            entries.add(new PieEntry((float) item.getAmount(), item.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Budget Allocation");
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Refresh chart
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar);
            binding.toolbar.setTitle(R.string.title_budget);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}