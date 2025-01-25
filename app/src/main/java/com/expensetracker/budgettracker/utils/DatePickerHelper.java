package com.expensetracker.budgettracker.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.TextView;
import java.util.Calendar;

public class DatePickerHelper {

    public static void showDatePicker(Context context, TextView dateInput) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dateInput.setText(formattedDate);
                },
                year, month, day
        );
        datePicker.show();
    }
}