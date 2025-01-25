package com.expensetracker.budgettracker.models;

public class BudgetItem {
    private final String category;
    private final double amount;

    public BudgetItem(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() { return category; }
    public double getAmount() { return amount; }
}