package co.edu.unipiloto.myapplication.storage // Corresponde a la ruta del archivo

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario en la aplicación utilizando [SharedPreferences].
 *
 * Esta clase proporciona una interfaz centralizada para guardar, recuperar y borrar
 * los datos de la sesión del usuario, como su ID, rol, zona y estado de autenticación.
 * Simplifica el manejo del estado de la sesión en toda la aplicación.
 *
 * @property context El contexto de la aplicación, necesario para acceder a SharedPreferences.
 * @constructor Crea una instancia de [SessionManager].
 */
class SessionManager(context: Context) {

    /**
     * Nombre del archivo de SharedPreferences donde se almacenan los datos de la sesión.
     * Es privado para evitar accesos desde fuera de la clase.
     */
    private val PREF_NAME = "LogiAppSession"

    // Claves para almacenar los datos en SharedPreferences. Usar constantes
    // previene errores de tipeo y centraliza los nombres de las claves.
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_ID = "userId"
    private val KEY_ROLE = "role"
    private val KEY_ZONA = "zona"

    /**
     * Instancia de [SharedPreferences] para leer los datos de la sesión.
     */
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Editor para escribir o modificar los datos en [SharedPreferences].
     */
    private val editor: SharedPreferences.Editor = pref.edit()

    /**
     * Create and persist a user login session with the given id, role, and optional zone.
     *
     * Marks the session as active and saves the provided user attributes for later retrieval.
     *
     * @param userId The unique identifier of the user.
     * @param role The user's role (for example "CLIENTE", "CONDUCTOR", "GESTOR").
     * @param zona The zone assigned to the user; may be null when not applicable.
     */
    fun createLoginSession(userId: Long, role: String, zona: String?) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_ZONA, zona)
        editor.apply() // Guarda los cambios de forma asíncrona.
    }

    /**
     * Clears the current user session.
     *
     * Removes all session data from SharedPreferences, deauthenticating the user.
     */
    fun logoutUser() {
        editor.clear() // Limpia todos los datos guardados.
        editor.apply()
    }

    /**
     * Checks whether a user session is active.
     *
     * @return `true` if a user is logged in, `false` otherwise.
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Gets the current session's user ID.
     *
     * @return The user ID, or -1 if no user is stored in the session.
     */
    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Gets the role of the current session user.
     *
     * @return The user's role as a String, or an empty string if none is stored.
     */
    fun getRole(): String {
        return pref.getString(KEY_ROLE, "") ?: ""
    }

    /**
     * Retrieve the zone assigned to the current user.
     *
     * @return The zone as a `String`, or `null` if no zone is assigned or there is no active session.
     */
    fun getZona(): String? {
        return pref.getString(KEY_ZONA, null)
    }
}