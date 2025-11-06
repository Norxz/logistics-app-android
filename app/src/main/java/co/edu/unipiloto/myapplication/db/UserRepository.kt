package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import co.edu.unipiloto.myapplication.models.LogisticUser // Asegúrate de tener este modelo
// Nota: Necesitarás implementar tu modelo LogisticUser para que compile

/**
 * Clase de datos que encapsula la información esencial de un usuario para la gestión de la sesión.
 *
 * @property id El ID único del usuario (de la tabla users).
 * @property role El rol del usuario (ej. "CLIENTE", "CONDUCTOR", "ADMIN").
 * @property sucursal La sucursal/zona asignada. Puede ser nulo.
 */
data class UserSessionData(
    val id: Long,
    val role: String,
    val sucursal: String?,
    val name: String // Nombre a usar en la UI
)

/**
 * Repositorio de datos para la gestión de usuarios, centralizando todas las interacciones con la BD.
 */
class UserRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)
    private val TAG = "UserRepository"

    // ==========================================================
    // 1. AUTENTICACIÓN (LOGIN) - SOPORTE MULTI-ROL UNIFICADO
    // ==========================================================

    /**
     * Autentica a un usuario verificando sus credenciales contra la tabla única de usuarios (users).
     *
     * @param email El email del usuario.
     * @param passwordHash El hash de la contraseña del usuario.
     * @return Un objeto [UserSessionData] si la autenticación es exitosa; de lo contrario, devuelve `null`.
     */
    fun login(email: String, passwordHash: String): UserSessionData? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var session: UserSessionData? = null
        val tableName = DBHelper.TABLE_USERS // Login solo en la tabla unificada de autenticación

        try {
            db = helper.readableDatabase

            // Columnas que buscamos en la tabla users:
            val selectCols = arrayOf("id", "name", "role", "sucursal")

            val query = "SELECT ${selectCols.joinToString(",")} FROM $tableName WHERE email = ? AND password_hash = ?"

            cursor = db.rawQuery(query, arrayOf(email, passwordHash))

            if (cursor.moveToFirst()) {
                val userId = cursor.getLong(0)
                val userName = cursor.getString(1)
                val role = cursor.getString(2)
                // Leer sucursal, que puede ser nula (índice 3)
                val sucursalIndex = cursor.getColumnIndexOrThrow("sucursal")
                val sucursal = if (!cursor.isNull(sucursalIndex)) {
                    cursor.getString(sucursalIndex)
                } else {
                    null
                }

                session = UserSessionData(userId, role, sucursal, userName)
                Log.d(TAG, "Login exitoso. Rol: $role")
            }
        } catch (e: Exception) {
            // Un error aquí podría ser si el esquema aún no está actualizado (Versión 10)
            Log.e(TAG, "Error de BD durante el login: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return session
    }

    // NOTA: Se eliminó la función findUserInTable ya que la lógica se unificó en login()

    // ==========================================================
    // 2. REGISTRO (CLIENTES)
    // ==========================================================

    /**
     * Registra un cliente directamente en la tabla de usuarios.
     * Añade ROLE='CLIENTE' y SUCURSAL='N/A'.
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
                put("name", fullName)
                put("phone_number", phoneNumber)
                put("role", "CLIENTE") // 🎯 Asignar rol
                put("sucursal", "N/A") // 🎯 Asignar sucursal por defecto
            }
            // Insertar en la tabla unificada de autenticación
            result = db.insertOrThrow(DBHelper.TABLE_USERS, null, cv)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar cliente: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    // ==========================================================
    // 3. REGISTRO (PERSONAL LOGÍSTICO / RECOLECTORES) - MODIFICADO
    // ==========================================================

    /**
     * Registra un nuevo miembro del personal logístico.
     * Inserta en la tabla users y luego en la tabla recolectores.
     */
    fun registerRecolector(
        name: String, // Usaremos name en lugar de username para consistencia con la tabla users
        email: String,
        passwordHash: String,
        role: String,
        sucursal: String?
    ): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        var recolectorId: Long = -1L

        try {
            // 1. Insertar en la tabla de autenticación (USERS)
            val userCV = ContentValues().apply {
                put("email", email)
                put("password_hash", passwordHash)
                put("name", name)
                put("phone_number", "") // Se puede añadir si el esquema lo permite
                put("role", role)
                put("sucursal", sucursal)
            }
            val userId = db.insertOrThrow(DBHelper.TABLE_USERS, null, userCV)

            if (userId > 0) {
                // 2. Insertar en la tabla específica (RECOLECTORES) con la FK
                val recolectorCV = ContentValues().apply {
                    put("user_id", userId)
                    put("is_active", 1)
                }
                recolectorId = db.insertOrThrow(DBHelper.TABLE_RECOLECTORES, null, recolectorCV)

                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar recolector (transacción): ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
        return recolectorId
    }

    // ==========================================================
    // 4. GESTIÓN DE PERSONAL LOGÍSTICO (CRUD ADMINISTRATIVO) - MODIFICADO
    // ==========================================================

    /**
     * Obtiene todos los usuarios logísticos (Recolectores) para el panel de administración.
     * Hace un JOIN entre USERS y RECOLECTORES.
     */
    fun getAllLogisticUsers(): List<LogisticUser> {
        val userList = mutableListOf<LogisticUser>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        try {
            db = helper.readableDatabase

            // JOIN entre la tabla de autenticación (Users) y la tabla específica (Recolectores)
            val query = """
                SELECT 
                    T2.id, T1.email, T1.name, T1.role, T1.sucursal, T1.phone_number, T2.is_active 
                FROM ${DBHelper.TABLE_USERS} T1
                INNER JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.id = T2.user_id
            """.trimIndent()

            cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0) // ID de la tabla RECOLECTORES (T2.id)
                val email = cursor.getString(1)
                val name = cursor.getString(2)
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val phoneNumber = cursor.getString(5)
                val isActive = cursor.getInt(6) == 1

                val user = LogisticUser(id, email, name, role, sucursal, phoneNumber, isActive)
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
     * Actualiza la tabla RECOLECTORES para el estado y la tabla USERS para el rol/sucursal.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    fun updateLogisticUserStatus(recolectorId: Long, isActive: Boolean): Boolean {
        val db = helper.writableDatabase
        var rowsUpdated = 0
        try {
            // Actualizamos la tabla RECOLECTORES usando su ID
            val cv = ContentValues().apply {
                put("is_active", if (isActive) 1 else 0)
            }
            rowsUpdated = db.update(
                DBHelper.TABLE_RECOLECTORES,
                cv,
                "id = ?",
                arrayOf(recolectorId.toString())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del recolector $recolectorId: ${e.message}")
        } finally {
            db.close()
        }
        return rowsUpdated > 0
    }

    /**
     * Elimina un usuario logístico por ID (debe ser una transacción que elimine de ambas tablas).
     */
    fun deleteLogisticUser(recolectorId: Long): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        var success = false
        try {
            // 1. Obtener el user_id de la tabla recolectores
            val cursor = db.rawQuery("SELECT user_id FROM ${DBHelper.TABLE_RECOLECTORES} WHERE id = ?", arrayOf(recolectorId.toString()))
            var userId: Long? = null
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0)
            }
            cursor.close()

            if (userId != null) {
                // 2. Eliminar de la tabla recolectores
                db.delete(DBHelper.TABLE_RECOLECTORES, "id = ?", arrayOf(recolectorId.toString()))

                // 3. Eliminar de la tabla users (el registro de autenticación)
                db.delete(DBHelper.TABLE_USERS, "id = ?", arrayOf(userId.toString()))

                success = true
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar usuario logístico (transacción) $recolectorId: ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }

    // ==========================================================
    // 5. OTRAS UTILIDADES - MODIFICADO
    // ==========================================================

    /**
     * Obtiene el nombre completo de un usuario por su ID (de la tabla USERS).
     */
    fun getFullNameById(id: Long): String? {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        var fullName: String? = null

        try {
            // Todos los nombres están en la tabla USERS
            cursor = db.rawQuery(
                "SELECT name FROM ${DBHelper.TABLE_USERS} WHERE id = ?",
                arrayOf(id.toString())
            )
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener el nombre por ID en ${DBHelper.TABLE_USERS}: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return fullName
    }

    /**
     * Obtiene una lista de conductores activos para el proceso de asignación manual.
     * Hace JOIN con la tabla USERS para obtener el nombre.
     */
    fun getDriversForAssignment(): List<Pair<Long, String>> {
        val drivers = mutableListOf<Pair<Long, String>>()
        val db = helper.readableDatabase
        var cursor: Cursor? = null

        val query = """
            SELECT T2.id, T1.name 
            FROM ${DBHelper.TABLE_USERS} T1 
            INNER JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.id = T2.user_id 
            WHERE T1.role = 'CONDUCTOR' AND T2.is_active = 1 
            ORDER BY T1.name ASC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0) // ID de la tabla RECOLECTORES
                val name = cursor.getString(1) // Nombre del conductor
                drivers.add(Pair(id, name))
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
     * encontrado en una zona específica.
     */
    fun getFirstRecolectorByZone(zona: String): LogisticUser? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var user: LogisticUser? = null

        try {
            db = helper.readableDatabase

            val query = """
                SELECT 
                    T2.id, T1.email, T1.name, T1.role, T1.sucursal, T1.phone_number, T2.is_active 
                FROM ${DBHelper.TABLE_USERS} T1
                INNER JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.id = T2.user_id
                WHERE T1.sucursal = ? AND T2.is_active = 1 LIMIT 1
            """.trimIndent()

            cursor = db.rawQuery(query, arrayOf(zona))

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val email = cursor.getString(1)
                val name = cursor.getString(2)
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val phoneNumber = cursor.getString(5)
                val isActive = cursor.getInt(6) == 1

                user = LogisticUser(id, email, name, role, sucursal, phoneNumber, isActive)
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