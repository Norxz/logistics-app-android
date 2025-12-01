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
    private val KEY_SUCURSAL = "sucursal_name" // (Nombre de la sucursal)
    // 🏆 CORRECCIÓN 1: Renombrar para coincidir con la convención del Fragmento
    private val KEY_BRANCH_ID = "branch_id"

    // Solo se necesita la instancia de SharedPreferences
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Crea y guarda una sesión de usuario después de un inicio de sesión exitoso.
     * * 💡 MEJORA: Se añade sucursalId como parámetro opcional para guardar todo en una transacción.
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

            // Guardar el ID de la sucursal en la misma transacción si está disponible
            if (sucursalId != null) {
                // Usar la clave corregida
                editor.putLong(KEY_BRANCH_ID, sucursalId)
            }

            editor.putString(KEY_NAME, name)
            editor.putString(KEY_EMAIL, email)
        }
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
     * NOTA: Este método ahora es redundante si se usa createLoginSession con sucursalId.
     */
    fun saveSucursalId(id: Long) {
        // Usar la clave corregida
        pref.edit().putLong(KEY_BRANCH_ID, id).apply()
    }

    /**
     * Obtiene el ID de la sucursal.
     * 🏆 CORRECCIÓN 2: Renombrado a getBranchId() para coincidir con el Fragmento.
     * Devuelve null si no está guardado.
     */
    fun getBranchId(): Long? {
        // Usar la clave corregida
        val id = pref.getLong(KEY_BRANCH_ID, -1L)
        // Usar -1L como valor por defecto para verificar si el ID existe realmente
        return id.takeIf { it != -1L }
    }

    // --- Cierre de Sesión ---

    /**
     * Cierra la sesión del usuario actual, eliminando todos los datos guardados.
     */
    fun logoutUser() {
        pref.edit().clear().apply()
    }

    /**
     * 🚨 CORRECCIÓN REQUERIDA POR AssignDriverActivity
     * Alias para getBranchId() para mantener la compatibilidad con el código cliente.
     */
    fun getSucursalId(): Long? {
        return getBranchId()
    }
}