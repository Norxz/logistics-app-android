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
     * Crea y guarda una sesión de usuario después de un inicio de sesión exitoso.
     *
     * Almacena el ID, el rol y la zona del usuario, y marca la sesión como activa.
     *
     * @param userId El ID único del usuario (proveniente de la base de datos).
     * @param role El rol del usuario (ej. "CLIENTE", "CONDUCTOR", "GESTOR").
     * @param zona La zona asignada al usuario. Es opcional y puede ser nulo, especialmente para clientes.
     */
    fun createLoginSession(userId: Long, role: String, zona: String?) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_ZONA, zona)
        editor.apply() // Guarda los cambios de forma asíncrona.
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * Borra todos los datos almacenados en SharedPreferences para esta sesión,
     * desautenticando efectivamente al usuario.
     */
    fun logoutUser() {
        editor.clear() // Limpia todos los datos guardados.
        editor.apply()
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     *
     * @return `true` si el usuario ha iniciado sesión, `false` en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Obtiene el ID del usuario de la sesión actual.
     *
     * @return El ID del usuario como [Long]. Devuelve -1 si no hay ningún usuario en sesión.
     */
    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Obtiene el rol del usuario de la sesión actual.
     *
     * @return El rol del usuario como [String]. Devuelve una cadena vacía si no se encuentra el rol.
     */
    fun getRole(): String {
        return pref.getString(KEY_ROLE, "") ?: ""
    }

    /**
     * Obtiene la zona asignada al usuario actual (generalmente para recolectores o gestores).
     *
     * @return La zona como un [String] nullable. Devuelve `null` si no hay una zona asignada o no hay sesión.
     */
    fun getZona(): String? {
        return pref.getString(KEY_ZONA, null)
    }
}
