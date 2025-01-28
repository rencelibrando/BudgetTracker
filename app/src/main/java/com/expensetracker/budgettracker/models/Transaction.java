package com.expensetracker.budgettracker.models;

import android.annotation.SuppressLint;

public class Transaction {
    private final String category;
    private final double amount;
    private final String date;
    private final String type;

    private int id;

    public Transaction(String category, double amount, String date, String type) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

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

    @SuppressLint("DefaultLocale")
    public String getFormattedAmount() {
        return String.format("₱%,.2f", amount);
    }

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
