package co.edu.unipiloto.myapplication.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario en la aplicación utilizando [SharedPreferences].
 */
class SessionManager(context: Context) {

    private val PREF_NAME = "LogiAppSession"

    // Claves para almacenar los datos
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_ID = "userId"
    private val KEY_ROLE = "role"
    private val KEY_ZONA = "zona"
    private val KEY_NAME = "name" // <-- CLAVE AÑADIDA PARA EL NOMBRE

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    /**
     * Crea y guarda una sesión de usuario después de un inicio de sesión exitoso.
     *
     * @param userId El ID único del usuario.
     * @param role El rol del usuario (ej. "CLIENTE", "CONDUCTOR", "ADMIN").
     * @param zona La zona asignada.
     * @param name El nombre visible del usuario. // <-- PARÁMETRO AÑADIDO
     */
    fun createLoginSession(userId: Long, role: String, zona: String?, name: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_ZONA, zona)
        editor.putString(KEY_NAME, name) // <-- GUARDAR EL NOMBRE
        editor.apply()
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logoutUser() {
        editor.clear()
        editor.apply()
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Obtiene el ID del usuario de la sesión actual.
     */
    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Obtiene el nombre del usuario de la sesión actual.
     * @return El nombre del usuario o una cadena vacía si no se encuentra.
     */
    fun getName(): String {
        return pref.getString(KEY_NAME, "") ?: ""
    }

    /**
     * Obtiene el rol del usuario de la sesión actual.
     */
    fun getRole(): String {
        return pref.getString(KEY_ROLE, "") ?: ""
    }

    /**
     * Obtiene la zona asignada al usuario actual.
     */
    fun getZona(): String? {
        return pref.getString(KEY_ZONA, null)
    }
}