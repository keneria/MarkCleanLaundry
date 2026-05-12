package com.example.markcleanlaundry;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Thin SharedPreferences wrapper for the logged-in user session.
 *
 * Usage:
 *   SessionManager sm = new SessionManager(context);
 *   sm.saveSession(userId, username);   // call this right after a successful DB login
 *   sm.getUserId()                      // returns -1 if not logged in
 *   sm.isLoggedIn()
 *   sm.logout()
 *
 * In your LoginActivity, after loginUser() cursor returns a row:
 *   int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
 *   String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME));
 *   new SessionManager(this).saveSession(userId, username);
 */
public class SessionManager {

    private static final String PREF_NAME    = "markclean_session";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "full_name";
    private static final int    NO_USER      = -1;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Call immediately after a successful login DB query. */
    public void saveSession(int userId, String username) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /** Optionally also cache the full name so you can greet the user without a DB query. */
    public void saveFullName(String fullName) {
        prefs.edit().putString(KEY_FULLNAME, fullName).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULLNAME, "");
    }

    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    /** Call on logout — clears everything. */
    public void logout() {
        prefs.edit().clear().apply();
    }
}