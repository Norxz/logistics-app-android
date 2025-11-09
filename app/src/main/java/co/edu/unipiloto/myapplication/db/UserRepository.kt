package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import co.edu.unipiloto.myapplication.models.LogisticUser
import co.edu.unipiloto.myapplication.models.Request // Importación necesaria para la nueva función

// NOTA: La clase UserSessionData debe estar en su propio archivo o solo en este.
data class UserSessionData(
    val id: Long,
    val role: String,
    val sucursal: String?,
    val name: String
)

/**
 * Repositorio de datos para la gestión de usuarios y solicitudes,
 * centralizando todas las interacciones con la BD.
 */
class UserRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)
    private val TAG = "UserRepository"

    // ==========================================================
    // 1. AUTENTICACIÓN (LOGIN)
    // ==========================================================
    fun login(email: String, passwordHash: String): UserSessionData? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var session: UserSessionData? = null
        val tableName = DBHelper.TABLE_USERS

        try {
            db = helper.readableDatabase
            val selectCols = arrayOf("id", "name", "role", "sucursal")
            val query =
                "SELECT ${selectCols.joinToString(",")} FROM $tableName WHERE email = ? AND password_hash = ?"
            cursor = db.rawQuery(query, arrayOf(email, passwordHash))

            if (cursor.moveToFirst()) {
                val userId = cursor.getLong(0)
                val userName = cursor.getString(1)
                val role = cursor.getString(2)
                val sucursalIndex = cursor.getColumnIndexOrThrow("sucursal")
                val sucursal =
                    if (!cursor.isNull(sucursalIndex)) cursor.getString(sucursalIndex) else null
                session = UserSessionData(userId, role, sucursal, userName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error de BD durante el login: ${e.message}")
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
                put("name", fullName)
                put("phone_number", phoneNumber)
                put("role", "CLIENTE")
                put("sucursal", "N/A")
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
    fun registerRecolector(
        name: String,
        email: String,
        passwordHash: String,
        role: String,
        sucursal: String?
    ): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        var recolectorId: Long = -1L

        try {
            val userCV = ContentValues().apply {
                put("email", email)
                put("password_hash", passwordHash)
                put("name", name)
                put("phone_number", "")
                put("role", role)
                put("sucursal", sucursal)
            }
            val userId = db.insertOrThrow(DBHelper.TABLE_USERS, null, userCV)

            if (userId > 0) {
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
    // 4. GESTIÓN DE PERSONAL LOGÍSTICO (CRUD ADMINISTRATIVO)
    // ==========================================================

    fun getAllLogisticUsers(): List<LogisticUser> {
        val userList = mutableListOf<LogisticUser>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        try {
            db = helper.readableDatabase
            val query = """
                SELECT
                    T2.id, T1.email, T1.name, T1.role, T1.sucursal, T1.phone_number, T2.is_active, T1.id AS user_id
                FROM ${DBHelper.TABLE_USERS} T1
                INNER JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.id = T2.user_id
            """.trimIndent()

            cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0) // T2.id (Recolector ID)
                val email = cursor.getString(1)
                val name = cursor.getString(2)
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val phoneNumber = cursor.getString(5)
                val isActive = cursor.getInt(6) == 1
                val userId = cursor.getLong(7) // T1.id (User FK)

                val user =
                    LogisticUser(id, email, name, role, sucursal, phoneNumber, isActive, userId)
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

    fun getLogisticUserById(recolectorId: Long): LogisticUser? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var user: LogisticUser? = null

        try {
            db = helper.readableDatabase
            val query = """
                SELECT
                    T2.id, T1.email, T1.name, T1.role, T1.sucursal, T1.phone_number, T2.is_active, T1.id AS user_id
                FROM ${DBHelper.TABLE_USERS} T1
                INNER JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.id = T2.user_id
                WHERE T2.id = ?
            """.trimIndent()

            cursor = db.rawQuery(query, arrayOf(recolectorId.toString()))

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val email = cursor.getString(1)
                val name = cursor.getString(2)
                val role = cursor.getString(3)
                val sucursal = cursor.getString(4)
                val phoneNumber = cursor.getString(5)
                val isActive = cursor.getInt(6) == 1
                val userId = cursor.getLong(7)

                user = LogisticUser(id, email, name, role, sucursal, phoneNumber, isActive, userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario logístico por ID: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return user
    }

    fun updateLogisticUser(user: LogisticUser): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        var success = false

        val userId = user.userId ?: run {
            Log.e(TAG, "Error: User ID (FK) no disponible para actualizar el usuario logístico.")
            return false
        }

        try {
            // 1. Actualizar la tabla USERS (Nombre, Email, Rol, Sucursal, Phone)
            val userCV = ContentValues().apply {
                put("name", user.name)
                put("email", user.email)
                put("role", user.role)
                put("sucursal", user.sucursal)
                put("phone_number", user.phoneNumber)
            }
            val userRows =
                db.update(DBHelper.TABLE_USERS, userCV, "id = ?", arrayOf(userId.toString()))

            // 2. Actualizar la tabla RECOLECTORES (Estado is_active)
            val recolectorCV = ContentValues().apply {
                put("is_active", if (user.isActive) 1 else 0)
            }
            val recolectorRows = db.update(
                DBHelper.TABLE_RECOLECTORES,
                recolectorCV,
                "id = ?",
                arrayOf(user.id.toString())
            )

            if (userRows > 0 && recolectorRows > 0) {
                db.setTransactionSuccessful()
                success = true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar usuario logístico (transacción): ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }

    fun updateLogisticUserStatus(recolectorId: Long, isActive: Boolean): Boolean {
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
                arrayOf(recolectorId.toString())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del recolector $recolectorId: ${e.message}")
        } finally {
            db.close()
        }
        return rowsUpdated > 0
    }

    fun deleteLogisticUser(recolectorId: Long): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        var success = false
        try {
            val cursor = db.rawQuery(
                "SELECT user_id FROM ${DBHelper.TABLE_RECOLECTORES} WHERE id = ?",
                arrayOf(recolectorId.toString())
            )
            var userId: Long? = null
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0)
            }
            cursor.close()

            if (userId != null) {
                db.delete(DBHelper.TABLE_RECOLECTORES, "id = ?", arrayOf(recolectorId.toString()))
                db.delete(DBHelper.TABLE_USERS, "id = ?", arrayOf(userId.toString()))
                success = true
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error al eliminar usuario logístico (transacción) $recolectorId: ${e.message}"
            )
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }

    // ==========================================================
    // 5. OTRAS UTILIDADES
    // ==========================================================

    fun getFullNameById(id: Long): String? {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        var fullName: String? = null
        try {
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
                val id = cursor.getLong(0)
                val name = cursor.getString(1)
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

    fun getFirstRecolectorByZone(zona: String): LogisticUser? {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var user: LogisticUser? = null

        try {
            db = helper.readableDatabase
            val query = """
                SELECT
                    T2.id, T1.email, T1.name, T1.role, T1.sucursal, T1.phone_number, T2.is_active, T1.id
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
                val userId = cursor.getLong(7) // T1.id (User FK)

                user = LogisticUser(id, email, name, role, sucursal, phoneNumber, isActive, userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener primer recolector por zona: ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return user
    }

    // ==========================================================
    // 6. GESTIÓN DE SOLICITUDES (CRUD ADMINISTRATIVO)
    // ==========================================================

    /**
     * Obtiene todas las solicitudes del sistema, incluyendo el nombre del conductor asignado (si lo hay).
     * Hace un LEFT JOIN entre SOLICITUDES, RECOLECTORES y USERS.
     */
    fun getAllRequests(): List<Request> {
        val requestList = mutableListOf<Request>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        try {
            db = helper.readableDatabase

            // T1.recolector_id es la clave foránea a RECOLECTORES.id
            val query = """
                SELECT
                    T1.id, T1.guia_id, T1.fecha, T1.estado, T1.created_at, 
                    T1.recolector_id, T3.name AS assigned_name,
                    T4.full_address, T3.name AS client_name_user, T5.tracking_number
                FROM ${DBHelper.TABLE_SOLICITUDES} T1
                
                -- JOIN para obtener la dirección completa
                INNER JOIN ${DBHelper.TABLE_DIRECCIONES} T4 ON T1.direccion_id = T4.id
                
                -- JOIN para obtener el número de tracking
                LEFT JOIN ${DBHelper.TABLE_GUIA} T5 ON T1.guia_id = T5.id
                
                -- JOIN para obtener el nombre del cliente (user_id en solicitudes apunta a USERS.id)
                INNER JOIN ${DBHelper.TABLE_USERS} T3 ON T1.user_id = T3.id 
                
                ORDER BY T1.created_at DESC
            """.trimIndent()

            cursor = db.rawQuery(query, null)

            // Nota: Aquí se asume que tu tabla SOLICITUDES tiene campos 'guia_id', 'fecha', 'estado', etc.
            // Si el modelo Request.kt tiene campos diferentes (como clientName, address, type),
            // la consulta debe adaptarse. La consulta anterior está muy simplificada.

            // VUELVO A USAR LA CONSULTA ORIGINAL DE LA RESPUESTA ANTERIOR (MÁS COMPLETA)
            // PERO CORRIGIENDO LOS NOMBRES DE COLUMNA DEL DBHelper

            val fullQuery = """
                SELECT 
                    T1.id, T5.tracking_number, T1.fecha, T1.estado, T4.full_address, 
                    T3.name AS client_name, T3.phone_number AS client_phone, T1.created_at,
                    T1.recolector_id, T6.name AS assigned_name
                FROM ${DBHelper.TABLE_SOLICITUDES} T1
                -- Cliente
                INNER JOIN ${DBHelper.TABLE_USERS} T3 ON T1.user_id = T3.id
                -- Dirección
                INNER JOIN ${DBHelper.TABLE_DIRECCIONES} T4 ON T1.direccion_id = T4.id
                -- Guía (tracking)
                LEFT JOIN ${DBHelper.TABLE_GUIA} T5 ON T1.guia_id = T5.id
                -- Recolector Asignado (JOIN a USERS a través de RECOLECTORES)
                LEFT JOIN ${DBHelper.TABLE_RECOLECTORES} T2 ON T1.recolector_id = T2.id
                LEFT JOIN ${DBHelper.TABLE_USERS} T6 ON T2.user_id = T6.id
                ORDER BY T1.created_at DESC
            """.trimIndent()

            cursor = db.rawQuery(fullQuery, null)

            // ... Mapeo del cursor a Request ...

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val guiaId = cursor.getString(1) ?: "N/A" // T5.tracking_number
                val type = "RECOLECCIÓN/ENVÍO" // No está en la BD, se asume
                val status = cursor.getString(3) // T1.estado
                val address = cursor.getString(4) // T4.full_address
                val clientName = cursor.getString(5) // T3.name

                val clientPhoneIndex = cursor.getColumnIndexOrThrow("client_phone")
                val clientPhone =
                    if (!cursor.isNull(clientPhoneIndex)) cursor.getString(clientPhoneIndex) else null

                val creationTimestamp = cursor.getString(7) // T1.created_at

                val recolectorIdIndex = cursor.getColumnIndexOrThrow("recolector_id")
                val assignedRecolectorId =
                    if (!cursor.isNull(recolectorIdIndex)) cursor.getLong(recolectorIdIndex) else null

                val recolectorNameIndex = cursor.getColumnIndexOrThrow("assigned_name")
                val assignedRecolectorName =
                    if (!cursor.isNull(recolectorNameIndex)) cursor.getString(recolectorNameIndex) else null

                val request = Request(
                    id = id,
                    guiaId = guiaId,
                    type = type, // Tienes que decidir dónde obtienes esto.
                    status = status,
                    address = address,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    creationTimestamp = creationTimestamp,
                    assignedRecolectorId = assignedRecolectorId,
                    assignedRecolectorName = assignedRecolectorName
                )
                requestList.add(request)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener todas las solicitudes (Corregido): ${e.message}")
        } finally {
            cursor?.close()
            db?.close()
        }
        return requestList
    }
}