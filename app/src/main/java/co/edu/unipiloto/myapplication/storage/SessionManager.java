package co.edu.unipiloto.myapplication.storage;

import android.content.Context;
import android.content.SharedPreferences;

import co.edu.unipiloto.myapplication.db.UserRepository;


public class SessionManager {
    private static final String PREF = "app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE    = "role";
    private static final String KEY_ZONA    = "zona";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveUser(long id, String role, String zona) {
        prefs.edit()
                .putLong(KEY_USER_ID, id)
                .putString(KEY_ROLE, role)
                .putString(KEY_ZONA, zona)
                .apply();
    }

    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1L); }
    public String getRole() { return prefs.getString(KEY_ROLE, null); }
    public String getZona() { return prefs.getString(KEY_ZONA, null); }

    public void clear() { prefs.edit().clear().apply(); }
}
