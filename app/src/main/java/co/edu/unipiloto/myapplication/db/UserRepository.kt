package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import co.edu.unipiloto.myapplication.models.LogisticUser // Asegúrate de tener este modelo

/**
 * Clase de datos que encapsula la información esencial de un usuario para la gestión de la sesión.
 *
 * @property id El ID único del usuario (de la tabla `users`, `recolectores` o `administrators`).
 * @property role El rol del usuario (ej. "CLIENTE", "CONDUCTOR", "ADMIN").
 * @property sucursal La sucursal/zona asignada. Puede ser nulo.
 */
data class UserSessionData(
    val id: Long,
    val role: String,
    val sucursal: String?,
    val name: String // Añadir nombre a la sesión para la UI
)

/**
 * Repositorio de datos para la gestión de usuarios, centralizando todas las interacciones con la BD.
 */
class UserRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)
    private val TAG = "UserRepository"

    // ==========================================================
    // 1. AUTENTICACIÓN (LOGIN) - SOPORTE MULTI-ROL (CLIENTE, LOGÍSTICO, ADMIN)
    // ==========================================================

    /**
     * Autentica a un usuario verificando sus credenciales contra las tres tablas.
     * La búsqueda prioriza el email para la mayoría de los roles.
     *
     * @param email El email del usuario (o username para el caso particular de recolectores si fuera el caso).
     * @param passwordHash El hash de la contraseña del usuario.
     * @return Un objeto [UserSessionData] si la autenticación es exitosa; de lo contrario, devuelve `null`.
     */
    fun login(email: String, passwordHash: String): UserSessionData? {
        // En tu esquema, todos tienen un 'email' excepto la tabla 'users' que usa 'full_name' y 'recolectores' que usa 'username'.
        // Vamos a usar 'email' como identificador principal para mayor coherencia con el Admin.

        // 1. Intentar como ADMINISTRADOR (usa email)
        val adminSession = findUserInTable(DBHelper.TABLE_ADMINS, email, passwordHash, "ADMIN")
        if (adminSession != null) return adminSession

        // 2. Intentar como PERSONAL LOGÍSTICO (usa email)
        val recolectorSession = findUserInTable(DBHelper.TABLE_RECOLECTORES, email, passwordHash, "LOGISTIC")
        if (recolectorSession != null) return recolectorSession

        // 3. Intentar como Cliente (usa email)
        val clientSession = findUserInTable(DBHelper.TABLE_USERS, email, passwordHash, "CLIENTE")
        if (clientSession != null) return clientSession

        return null
    }

    /**
     * Función genérica para buscar y autenticar en una tabla específica.
     */
    private fun findUserInTable(tableName: String, email: String, hashedPassword: String, roleType: String): UserSessionData? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var session: UserSessionData? = null

        try {
            db = helper.readableDatabase

            // Determinar qué columna usar para buscar el identificador y el nombre visible
            val identifierColumn = "email" // Usamos email como estándar para login
            val nameColumn = when (tableName) {
                DBHelper.TABLE_USERS -> "name" // Tu esquema usa 'name'
                DBHelper.TABLE_RECOLECTORES -> "username"
                DBHelper.TABLE_ADMINS -> "name"
                else -> "name"
            }

            val zonaColumn = if (tableName == DBHelper.TABLE_RECOLECTORES) "sucursal" else null

            val selectCols = mutableListOf("id", nameColumn)
            if (zonaColumn != null) selectCols.add(zonaColumn)

            val query = "SELECT ${selectCols.joinToString(",")} FROM $tableName WHERE $identifierColumn = ? AND password_hash = ?"

            cursor = db.rawQuery(query, arrayOf(email, hashedPassword))

            if (cursor.moveToFirst()) {
                val userId = cursor.getLong(0)
                val userName = cursor.getString(1)
                // Se modificó la lectura para evitar el error de índice si zonaColumn es null
                val zona = if (zonaColumn != null && !cursor.isNull(cursor.getColumnIndexOrThrow(zonaColumn))) {
                    cursor.getString(cursor.getColumnIndexOrThrow(zonaColumn))
                } else {
                    null
                }

                session = UserSessionData(userId, roleType, zona, userName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error de BD al buscar en $tableName: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return session
    }

    // ==========================================================
    // 2. REGISTRO (CLIENTES)
    // ==========================================================

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
                put("name", fullName) // Usando 'name' como en tu nuevo esquema
                put("phone_number", phoneNumber)
            }
            result = db.insertOrThrow(DBHelper.TABLE_USERS, null, cv)
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
     * Registra un nuevo miembro del personal logístico.
     */
    fun registerRecolector(
        username: String,
        email: String, // Añadido email para cumplir con el esquema
        passwordHash: String,
        role: String,
        sucursal: String? // Usando 'sucursal' como en tu nuevo esquema
    ): Long {
        val db = helper.writableDatabase
        var result: Long = -1L
        try {
            val cv = ContentValues().apply {
                put("username", username)
                put("email", email) // El esquema requiere email
                put("password_hash", passwordHash)
                put("role", role)
                put("sucursal", sucursal) // Usando sucursal
                put("is_active", 1)
            }
            result = db.insertOrThrow(DBHelper.TABLE_RECOLECTORES, null, cv)
            Log.d(TAG, "Registro de recolector exitoso, ID: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar recolector: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    // ==========================================================
    // 4. GESTIÓN DE PERSONAL LOGÍSTICO (CRUD ADMINISTRATIVO)
    // ==========================================================

    /**
     * Obtiene todos los usuarios logísticos (Recolectores) para el panel de administración.
     */
    fun getAllLogisticUsers(): List<LogisticUser> {
        val userList = mutableListOf<LogisticUser>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        try {
            db = helper.readableDatabase
            // Nota: Tu esquema no tiene 'phone_number', usamos un valor vacío
            val query = "SELECT id, email, username, role, sucursal, is_active FROM ${DBHelper.TABLE_RECOLECTORES}"
            cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val email = cursor.getString(1)
                val name = cursor.getString(2) // username como nombre
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val isActive = cursor.getInt(5) == 1

                // PhoneNumber se deja vacío ya que no está en la tabla Recolectores
                val user = LogisticUser(id, email, name, role, sucursal, "", isActive)
                userList.add(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuarios logísticos: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return userList
    }

    /**
     * Actualiza el estado (activo/inactivo) o cualquier otro campo de un usuario logístico.
     * Este método es vital para el panel de gestión.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    fun updateLogisticUserStatus(userId: Long, isActive: Boolean): Boolean {
        val db = helper.writableDatabase
        var rowsUpdated = 0
        try {
            val cv = ContentValues().apply {
                put("is_active", if (isActive) 1 else 0)
            }
            rowsUpdated = db.update(
                DBHelper.TABLE_RECOLECTORES,
                cv,
                "id = ?",
                arrayOf(userId.toString())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del usuario $userId: ${e.message}")
        } finally {
            db.close()
        }
        return rowsUpdated > 0
    }

    /**
     * Elimina un usuario logístico por ID.
     */
    fun deleteLogisticUser(userId: Long): Boolean {
        val db = helper.writableDatabase
        var rowsDeleted = 0
        try {
            rowsDeleted = db.delete(
                DBHelper.TABLE_RECOLECTORES,
                "id = ?",
                arrayOf(userId.toString())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar usuario logístico $userId: ${e.message}")
        } finally {
            db.close()
        }
        return rowsDeleted > 0
    }

    // ==========================================================
    // 5. OTRAS UTILIDADES
    // ==========================================================

    /**
     * Obtiene el nombre completo de un usuario por su ID y rol.
     */
    fun getFullNameById(id: Long, role: String): String? {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        var fullName: String? = null
        val tableName: String
        val nameColumn: String

        when (role) {
            "ADMIN" -> { tableName = DBHelper.TABLE_ADMINS; nameColumn = "name" }
            "CLIENTE" -> { tableName = DBHelper.TABLE_USERS; nameColumn = "name" }
            "CONDUCTOR", "GESTOR", "FUNCIONARIO", "ANALISTA", "LOGISTIC" -> {
                tableName = DBHelper.TABLE_RECOLECTORES; nameColumn = "username"
            }
            else -> return null
        }

        try {
            cursor = db.rawQuery(
                "SELECT $nameColumn FROM $tableName WHERE id = ?",
                arrayOf(id.toString())
            )
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener el nombre por ID en $tableName: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return fullName
    }

    /**
     * Obtiene una lista de conductores activos para el proceso de asignación manual.
     */
    fun getDriversForAssignment(): List<Pair<Long, String>> {
        val drivers = mutableListOf<Pair<Long, String>>()
        val db = helper.readableDatabase
        var cursor: Cursor? = null

        val query = "SELECT id, username FROM ${DBHelper.TABLE_RECOLECTORES} WHERE role = 'CONDUCTOR' AND is_active = 1 ORDER BY username ASC"

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

    /**
     * Obtiene el primer usuario logístico (recolector/conductor) ACTIVO
     * encontrado en una zona específica. Utilizado para la asignación automática
     * por parte del gestor.
     *
     * @param zona La zona (sucursal) a buscar.
     * @return El objeto [LogisticUser] del primer recolector encontrado, o null.
     */
    fun getFirstRecolectorByZone(zona: String): LogisticUser? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var user: LogisticUser? = null

        try {
            db = helper.readableDatabase

            // Buscamos un recolector o conductor activo en la zona
            val query = "SELECT id, email, username, role, sucursal, is_active FROM ${DBHelper.TABLE_RECOLECTORES} WHERE sucursal = ? AND is_active = 1 LIMIT 1"
            cursor = db.rawQuery(query, arrayOf(zona))

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val email = cursor.getString(1)
                val name = cursor.getString(2) // Usando username como nombre visible
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val isActive = cursor.getInt(5) == 1

                // PhoneNumber se deja vacío ya que no está en la tabla Recolectores
                user = LogisticUser(id, email, name, role, sucursal, "", isActive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener primer recolector por zona: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return user
    }
}