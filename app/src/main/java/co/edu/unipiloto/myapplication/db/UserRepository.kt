package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

/**
 * Clase de datos que encapsula la información esencial de un usuario para la gestión de la sesión.
 *
 * Se utiliza como un objeto de transferencia de datos (DTO) después de un inicio de sesión exitoso,
 * conteniendo la información mínima necesaria para establecer el contexto del usuario en la aplicación.
 *
 * @property id El ID único del usuario (de la tabla `users` o `recolectores`).
 * @property role El rol del usuario (ej. "CLIENTE", "CONDUCTOR", "GESTOR").
 * @property zona La zona asignada al usuario. Puede ser nulo, especialmente para los clientes.
 */
data class UserSessionData(
    val id: Long,
    val role: String,
    val zona: String?
)

/**
 * Repositorio de datos para la gestión de usuarios, tanto clientes como personal logístico.
 *
 * Esta clase centraliza todas las interacciones con la base de datos relacionadas con las tablas `users`
 * y `recolectores`. Proporciona métodos para la autenticación y el registro, abstrayendo
 * la lógica de las consultas SQL del resto de la aplicación.
 *
 * @property helper Instancia de [DBHelper] para acceder a la base de datos SQLite.
 * @constructor Crea una instancia del repositorio.
 * @param context El contexto de la aplicación, necesario para inicializar [DBHelper].
 */
class UserRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)
    private val TAG = "UserRepository"

    // ==========================================================
    // 1. AUTENTICACIÓN (LOGIN)
    // ==========================================================

    /**
     * Autentica a un usuario verificando sus credenciales contra las tablas `users` y `recolectores`.
     *
     * La lógica de autenticación es polimórfica:
     * 1. Primero, intenta encontrar un cliente en la tabla `users` usando el `usernameOrEmail` como email.
     * 2. Si no lo encuentra, intenta encontrar un miembro del personal en la tabla `recolectores` usando
     *    `usernameOrEmail` como `username`.
     *
     * @param usernameOrEmail El identificador del usuario, que puede ser un email (para clientes) o un nombre de usuario (para personal).
     * @param passwordHash El hash de la contraseña del usuario.
     * @return Un objeto [UserSessionData] con los datos de sesión si la autenticación es exitosa; de lo contrario, devuelve `null`.
     */
    fun login(usernameOrEmail: String, passwordHash: String): UserSessionData? {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        var userData: UserSessionData? = null

        try {
            // 1. Intentar como Cliente (tabla users) usando 'email'
            cursor = db.rawQuery(
                "SELECT id FROM users WHERE email = ? AND password_hash = ?",
                arrayOf(usernameOrEmail, passwordHash)
            )
            if (cursor.moveToFirst()) {
                val userId = cursor.getLong(0)
                userData = UserSessionData(userId, "CLIENTE", null)
                // Si se encontró un cliente, retornar inmediatamente para optimizar
                return userData
            }
            cursor.close() // Cerrar el cursor después de cada consulta

            // 2. Intentar como Personal Logístico (tabla recolectores) usando 'username'
            cursor = db.rawQuery(
                "SELECT id, role, zona FROM recolectores WHERE username = ? AND password_hash = ? AND is_active = 1",
                arrayOf(usernameOrEmail, passwordHash)
            )
            if (cursor.moveToFirst()) {
                val recolectorId = cursor.getLong(0)
                val role = cursor.getString(1)
                val zona = cursor.getString(2)
                userData = UserSessionData(recolectorId, role, zona)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error durante el inicio de sesión: ${e.message}")
            userData = null // Asegurarse de que se devuelve null en caso de error
        } finally {
            cursor?.close()
            db.close()
        }

        return userData
    }

    // ==========================================================
    // 2. REGISTRO (CLIENTES)
    // ==========================================================

    /**
     * Registra un nuevo cliente en la tabla `users`.
     *
     * @param email El email del nuevo cliente. Debe ser único.
     * @param passwordHash El hash de la contraseña del cliente.
     * @param fullName El nombre completo del cliente.
     * @param phoneNumber El número de teléfono del cliente.
     * @return El ID de la fila del nuevo cliente insertado, o -1L si el registro falla (por ejemplo, si el email ya existe).
     */
    fun registerClient(
        email: String,
        passwordHash: String,
        fullName: String,
        phoneNumber: String
    ): Long {
        val db = helper.writableDatabase
        var result: Long = -1L
        try {
            val cv = ContentValues().apply {
                put("email", email)
                put("password_hash", passwordHash)
                put("full_name", fullName)
                put("phone_number", phoneNumber)
            }
            result = db.insertOrThrow("users", null, cv)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar cliente: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    // ==========================================================
    // 3. REGISTRO (PERSONAL LOGÍSTICO / RECOLECTORES)
    // ==========================================================

    /**
     * Registra un nuevo miembro del personal logístico en la tabla `recolectores`.
     *
     * @param username El nombre de usuario para el nuevo miembro del personal. Debe ser único.
     * @param passwordHash El hash de la contraseña.
     * @param role El rol asignado (ej. "RECOLECTOR", "GESTOR").
     * @param zona La zona geográfica asignada. Puede ser nula.
     * @return El ID de la nueva fila insertada, o -1L si el registro falla (por ejemplo, si el `username` ya existe).
     */
    fun registerRecolector(
        username: String,
        passwordHash: String,
        role: String,
        zona: String?
    ): Long {
        val db = helper.writableDatabase
        var result: Long = -1L
        try {
            val cv = ContentValues().apply {
                put("username", username)
                put("password_hash", passwordHash)
                put("role", role)
                put("zona", zona)
                put("is_active", 1) // Por defecto, el nuevo personal está activo.
            }
            result = db.insertOrThrow("recolectores", null, cv)
            Log.d(TAG, "Registro de recolector exitoso, ID: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar recolector: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    /**
     * Obtiene el nombre completo o nombre de usuario visible de un usuario por su ID.
     *
     * La lógica es polimórfica y busca en dos tablas:
     * 1. Primero, busca en la tabla `recolectores` por el `username`.
     * 2. Si no se encuentra, busca en la tabla `users` por el `full_name`.
     *
     * Este método es útil para mostrar el nombre del usuario en la UI sin necesidad
     * de saber de antemano si es un cliente o un miembro del personal.
     *
     * @param id El ID del usuario a buscar.
     * @return El `username` (para personal) o `full_name` (para clientes) como un [String],
     *         o `null` si no se encuentra ningún usuario con ese ID en ninguna de las dos tablas.
     */
    fun getFullNameById(id: Long): String? {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        var fullName: String? = null

        try {
            // 1. Intentar buscar en la tabla de Recolectores (usando 'username' como nombre visible)
            cursor = db.rawQuery(
                "SELECT username FROM recolectores WHERE id = ?",
                arrayOf(id.toString())
            )
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(0)
                return fullName // Devolver tan pronto como se encuentre
            }
            cursor.close()

            // 2. Si no es un recolector, intentar buscar en la tabla de Clientes (usando 'full_name')
            cursor = db.rawQuery(
                "SELECT full_name FROM users WHERE id = ?",
                arrayOf(id.toString())
            )
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener el nombre por ID: ${e.message}")
            fullName = null // Asegurarse de devolver null en caso de error
        } finally {
            cursor?.close()
            db.close()
        }
        return fullName
    }
}