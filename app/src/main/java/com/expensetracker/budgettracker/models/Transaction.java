package com.expensetracker.budgettracker.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Transaction {
    private final String category;
    private final double amount; // Changed to double for easier calculations
    private final String date; // Format: "YYYY-MM-DD"
    private final String type; // "income" or "expense"

    // Constructor
    public Transaction(String category, double amount, String date, String type) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }
    // In Transaction.java
    private int id;

    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    // Getters
    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    // Utility Methods
    @SuppressLint("DefaultLocale")
    public String getFormattedAmount() {
        return String.format("₱%,.2f", amount); // Format as currency with commas
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
