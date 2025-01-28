package com.expensetracker.budgettracker.models;

public class Flashcard {
    private final int iconResId;
    private final String label;
    private String amount;

    public Flashcard(int iconResId, String label, String amount) {
        this.iconResId = iconResId;
        this.label = label;
        this.amount = amount;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getLabel() {
        return label;
    }

    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "iconResId=" + iconResId +
                ", label='" + label + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
