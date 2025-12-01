package co.edu.unipiloto.myapplication.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Extensión para SharedPreferences.Editor que permite agrupar transacciones de put/apply.
 * Evita llamar a .edit() y .apply() manualmente para cada cambio.
 */
inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = edit()
    operation(editor)
    editor.apply()
}

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

    // Claves específicas de personal logístico (SOLO SUCURSAL)
    private val KEY_SUCURSAL = "sucursal_name"
    private val KEY_BRANCH_ID = "branch_id"

    // Solo se necesita la instancia de SharedPreferences
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Crea y guarda una sesión de usuario después de un inicio de sesión exitoso.
     */
    fun createLoginSession(
        userId: Long,
        role: String,
        sucursal: String?,
        sucursalId: Long?,
        name: String,
        email: String
    ) {
        pref.edit { editor ->
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putLong(KEY_USER_ID, userId)
            editor.putString(KEY_ROLE, role)
            editor.putString(KEY_SUCURSAL, sucursal)

            if (sucursalId != null) {
                editor.putLong(KEY_BRANCH_ID, sucursalId)
            }

            editor.putString(KEY_NAME, name)
            editor.putString(KEY_EMAIL, email)
        }
    }

    // --- Getters de Sesión ---

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1L)
    }

    fun getName(): String {
        return pref.getString(KEY_NAME, "") ?: ""
    }

    fun getUserFullName(): String? {
        return pref.getString(KEY_NAME, null)
    }

    fun getRole(): String {
        return pref.getString(KEY_ROLE, "") ?: ""
    }

    fun getSucursal(): String? {
        return pref.getString(KEY_SUCURSAL, null)
    }

    fun getUserEmail(): String? {
        return pref.getString(KEY_EMAIL, null)
    }

    // --- Métodos de Sucursal ---

    fun saveSucursalId(id: Long) {
        pref.edit().putLong(KEY_BRANCH_ID, id).apply()
    }

    fun getBranchId(): Long? {
        val id = pref.getLong(KEY_BRANCH_ID, -1L)
        return id.takeIf { it != -1L }
    }

    fun getSucursalId(): Long? {
        return getBranchId()
    }

    // --- Cierre de Sesión ---

    /**
     * Cierra la sesión del usuario actual, eliminando todos los datos guardados.
     */
    fun logoutUser() {
        pref.edit().clear().apply()
    }

    // 📢 MÉTODO AÑADIDO: Alias para logoutUser()
    fun logout() {
        logoutUser()
    }
}