package com.expensetracker.budgettracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "BudgetTrackerPref";
    private static final String KEY_USER_ID = "user_id";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    // In SessionManager.java
    public boolean isLoggedIn() {
        return pref.contains(KEY_USER_ID);
    }
    public void loginUser(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
