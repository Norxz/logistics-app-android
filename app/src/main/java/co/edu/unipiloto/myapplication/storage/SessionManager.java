package co.edu.unipiloto.myapplication.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "app_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
