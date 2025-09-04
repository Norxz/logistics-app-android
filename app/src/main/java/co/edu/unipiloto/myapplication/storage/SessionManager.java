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

    public void saveUserId(long id){ prefs.edit().putLong("user_id", id).apply(); }
    public long getUserId(){ return prefs.getLong("user_id", -1L); }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
