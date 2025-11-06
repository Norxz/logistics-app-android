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
     * Authenticate a user by validating credentials against the clients and logistics staff tables.
     *
     * First attempts to match a client using the value as an email; if not found, attempts to match an active logistics staff member using the value as a username.
     *
     * @param usernameOrEmail Identifier provided by the user; treated as an email when matching clients and as a username when matching logistics staff.
     * @param passwordHash Hash of the user's password.
     * @return `UserSessionData` with session information when credentials match a client or an active logistics staff member, `null` otherwise.
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
     * Insert a new client into the `users` table.
     *
     * @param email The client's email; expected to be unique.
     * @param passwordHash The hashed password for the client.
     * @param fullName The client's full name.
     * @param phoneNumber The client's phone number.
     * @return The row ID of the newly inserted client, or `-1L` if insertion fails.
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
     * Inserts a logistics staff member into the `recolectores` table.
     *
     * The new record is created as active by default (is_active = 1).
     *
     * @param username The username for the staff member; should be unique.
     * @param passwordHash The password hash to store.
     * @param role The assigned role (e.g., "CONDUCTOR", "RECOLECTOR").
     * @param zona The assigned geographic zone, or `null` if none.
     * @return The row ID of the inserted record, or -1L if the insertion fails.
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
     * Retrieves the display name for a user by ID, preferring a staff member's `username` over a client's `full_name`.
     *
     * @param id The user ID to look up.
     * @return The staff `username` or the client's `full_name`, or `null` if no matching user is found.
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

    /**
     * Retrieves active drivers and their usernames for assignment.
     *
     * @return A list of pairs where the first element is the driver's ID and the second is the username; the list is ordered by username. An empty list is returned if there are no matching drivers or an error occurs.
     */
    fun getDriversForAssignment(): List<Pair<Long, String>> {
        val drivers = mutableListOf<Pair<Long, String>>()
        val db = helper.readableDatabase
        var cursor: Cursor? = null

        val query = "SELECT id, username FROM recolectores WHERE role = 'CONDUCTOR' AND is_active = 1 ORDER BY username ASC"

        try {
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val usernameIndex = cursor.getColumnIndexOrThrow("username")

                do {
                    val id = cursor.getLong(idIndex)
                    val username = cursor.getString(usernameIndex)
                    drivers.add(Pair(id, username))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener lista de conductores: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return drivers
    }

}