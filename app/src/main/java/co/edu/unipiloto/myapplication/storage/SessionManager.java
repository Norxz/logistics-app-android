package co.edu.unipiloto.myapplication.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestiona los datos de la sesión del usuario utilizando SharedPreferences.
 * Esta clase proporciona métodos para guardar, recuperar y borrar la información de la sesión del usuario.
 */
public class SessionManager {
    private static final String PREF = "app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE    = "role";
    private static final String KEY_ZONA    = "zona";

    private final SharedPreferences prefs;

    /**
     * Construye un nuevo SessionManager.
     *
     * @param ctx El contexto de la aplicación.
     */
    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /**
     * Guarda los datos de la sesión del usuario.
     *
     * @param id El ID del usuario.
     * @param role El rol del usuario.
     * @param zona La zona del usuario.
     */
    public void saveUser(long id, String role, String zona) {
        prefs.edit()
                .putLong(KEY_USER_ID, id)
                .putString(KEY_ROLE, role)
                .putString(KEY_ZONA, zona)
                .apply();
    }

    /**
     * Obtiene el ID del usuario actual.
     *
     * @return El ID del usuario, o -1 si no ha iniciado sesión.
     */
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1L); }

    /**
     * Obtiene el rol del usuario actual.
     *
     * @return El rol del usuario, o null si no ha iniciado sesión.
     */
    public String getRole() { return prefs.getString(KEY_ROLE, null); }

    /**
     * Obtiene la zona del usuario actual.
     *
     * @return La zona del usuario, o null si no ha iniciado sesión.
     */
    public String getZona() { return prefs.getString(KEY_ZONA, null); }

    /**
     * Limpia la sesión del usuario actual.
     */
    public void clear() { prefs.edit().clear().apply(); }
}
