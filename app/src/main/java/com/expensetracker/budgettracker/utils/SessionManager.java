package com.expensetracker.budgettracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static final String PREF_NAME = "BudgetTrackerPref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Modified to use long for user IDs
    public void loginUser(long userId, String username) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.contains(KEY_USER_ID) && pref.getLong(KEY_USER_ID, -1) != -1;
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public void logoutUser() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.apply();
    }
}