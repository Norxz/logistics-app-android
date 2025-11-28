package co.edu.unipiloto.myapplication.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * 💾 Gestor de Sesión del Usuario.
 * Utiliza SharedPreferences para almacenar de forma persistente
 * los datos de la sesión (ID, rol, sucursal, etc.) del usuario logueado.
 */
class SessionManager(context: Context) {

    private val PREF_NAME = "LogiAppSession"

    // Claves de la sesión
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_ID = "userId"
    private val KEY_ROLE = "role"
    private val KEY_NAME = "name"
    private val KEY_EMAIL = "USER_EMAIL"

    // Claves específicas de personal logístico (usamos SUCURSAL)
    private val KEY_SUCURSAL = "sucursal_name" // (Nombre de la sucursal)
    private val KEY_SUCURSAL_ID = "sucursal_id" // (ID numérico de la sucursal)

    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    /**
     * Crea y guarda una sesión de usuario después de un inicio de sesión exitoso.
     *
     * @param userId El ID único del usuario (Long).
     * @param role El rol del usuario (String, ej. "CLIENTE", "CONDUCTOR").
     * @param sucursal El nombre de la sucursal asignada (String?, puede ser null para Clientes).
     * @param name El nombre visible del usuario (String).
     * @param email El correo electrónico del usuario (String).
     */
    fun createLoginSession(userId: Long, role: String, sucursal: String?, name: String, email: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)

        // 🚨 CAMBIO CLAVE: Guardamos el nombre de la sucursal usando KEY_SUCURSAL
        editor.putString(KEY_SUCURSAL, sucursal)

        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    // --- Getters de Sesión ---

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
     * Obtiene el nombre completo del usuario.
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
     * Obtiene el nombre de la sucursal asignada al usuario.
     */
    fun getSucursal(): String? {
        // 🚨 CAMBIO CLAVE: Usa la constante KEY_SUCURSAL
        return pref.getString(KEY_SUCURSAL, null)
    }

    /**
     * Obtiene el correo electrónico del usuario.
     */
    fun getUserEmail(): String? {
        return pref.getString(KEY_EMAIL, null)
    }

    // --- Métodos de Sucursal ---

    /**
     * Guarda el ID de la sucursal del usuario (útil para el personal logístico).
     */
    fun saveSucursalId(id: Long) {
        pref.edit().putLong(KEY_SUCURSAL_ID, id).apply()
    }

    /**
     * Obtiene el ID de la sucursal.
     * Devuelve null si no está guardado.
     */
    fun getSucursalId(): Long? {
        val id = pref.getLong(KEY_SUCURSAL_ID, -1L)
        // Devuelve null si el valor es el default (-1L)
        return id.takeIf { it != -1L }
    }

    // --- Cierre de Sesión ---

    /**
     * Cierra la sesión del usuario actual, eliminando todos los datos guardados.
     */
    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}