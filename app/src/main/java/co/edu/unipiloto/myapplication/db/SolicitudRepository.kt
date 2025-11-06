package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Repositorio encargado de gestionar las operaciones CRUD (Crear, Leer, Actualizar, Borrar) para las solicitudes de envío.
 */
class SolicitudRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)

    // ==========================================================
    // DATA CLASSES: Representación de las tablas/ítems
    // ==========================================================

    /**
     * Representa una solicitud de envío completa con todos sus campos.
     */
    data class Solicitud(
        val id: Long,
        val userId: Long,
        val recolectorId: Long?,
        val direccionId: Long,
        val fecha: String,
        val franja: String,
        val notas: String,
        val zona: String,
        val guiaId: Long?,
        val estado: String,
        val confirmationCode: String?,
        val createdAt: Long
    )

    /**
     * Representa una vista simplificada de una solicitud para listados de UI.
     */
    data class SolicitudItem(
        val id: Long,
        val direccion: String,
        val fecha: String,
        val estado: String
    )

    // ==========================================================
    // OPERACIONES CRUD / CONSULTAS
    /**
     * Creates a new address and an associated delivery request (solicitud) in a single database transaction.
     *
     * The created solicitud is initialized with estado "PENDIENTE" and the address city is set to "Bogotá".
     *
     * @param userId The ID of the user who owns the solicitud.
     * @param direccionCompleta The full address text for the associated direccion.
     * @param fechaRecoleccion The requested collection date (as stored in the database).
     * @param franjaHoraria The requested time slot for collection.
     * @param notas Additional notes for the solicitud.
     * @param zona The delivery/collection zone identifier.
     * @return The newly inserted solicitud ID, or -1 if the creation failed.
     */

    fun crear(
        userId: Long,
        direccionCompleta: String,
        fechaRecoleccion: String,
        franjaHoraria: String,
        notas: String,
        zona: String
    ): Long {
        val db = helper.writableDatabase
        var newSolicitudId: Long = -1L
        var newDireccionId: Long = -1L
        db.beginTransaction()
        try {
            // 1. CREAR LA DIRECCIÓN ASOCIADA
            val cvDireccion = ContentValues().apply {
                put("user_id", userId)
                put("ciudad", "Bogotá") // Valor fijo por ahora
                put("full_address", direccionCompleta)
                put("created_at", System.currentTimeMillis())
                put("latitude", 0.0) // Placeholder
                put("longitude", 0.0) // Placeholder
            }
            newDireccionId = db.insert("direcciones", null, cvDireccion)

            if (newDireccionId == -1L) throw Exception("Error al insertar la dirección.")

            // 2. CREAR LA SOLICITUD CON LA FK DE LA DIRECCIÓN
            val cvSolicitud = ContentValues().apply {
                put("user_id", userId)
                put("direccion_id", newDireccionId)
                put("fecha", fechaRecoleccion)
                put("franja", franjaHoraria)
                put("notas", notas)
                put("zona", zona)
                put("estado", "PENDIENTE") // Estado inicial por defecto
                put("created_at", System.currentTimeMillis())
            }
            newSolicitudId = db.insert("solicitudes", null, cvSolicitud)

            if (newSolicitudId == -1L) throw Exception("Error al insertar la solicitud.")

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("Repo", "Transacción fallida al crear solicitud: ${e.message}")
            newSolicitudId = -1L
        } finally {
            db.endTransaction()
            db.close()
        }
        return newSolicitudId
    }

    /**
     * Retrieves the list of solicitudes for the given user, ordered by creation time (most recent first).
     *
     * @param userId ID of the user whose solicitudes are being queried.
     * @return A list of `SolicitudItem` containing id, address, date, and status for the user, or `null` if an error occurs.
     */
    fun listarPorUsuario(userId: Long): List<SolicitudItem>? {
        val db = helper.readableDatabase
        val items = mutableListOf<SolicitudItem>()
        var cursor: Cursor? = null

        val query = """
            SELECT 
                s.id, 
                d.full_address, 
                s.fecha, 
                s.estado
            FROM 
                solicitudes s
            INNER JOIN 
                direcciones d ON s.direccion_id = d.id
            WHERE 
                s.user_id = ?
            ORDER BY 
                s.created_at DESC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val addressIndex = cursor.getColumnIndexOrThrow("full_address")
                val dateIndex = cursor.getColumnIndexOrThrow("fecha")
                val statusIndex = cursor.getColumnIndexOrThrow("estado")

                do {
                    val item = SolicitudItem(
                        id = cursor.getLong(idIndex),
                        direccion = cursor.getString(addressIndex),
                        fecha = cursor.getString(dateIndex),
                        estado = cursor.getString(statusIndex)
                    )
                    items.add(item)
                } while (cursor.moveToNext())
            }
            return items
        } catch (e: Exception) {
            Log.e("Repo", "Error al listar solicitudes: ${e.message}")
            return null
        } finally {
            cursor?.close()
            db.close()
        }
    }

    /**
     * Cancela la solicitud indicada para el usuario especificado cambiando su estado a "CANCELADA" si actualmente está en "PENDIENTE".
     *
     * @param solicitudId ID de la solicitud a cancelar.
     * @param userId ID del usuario propietario de la solicitud.
     * @return El número de filas actualizadas (`1` si la solicitud fue cancelada, `0` si no se encontró o no estaba en estado "PENDIENTE").
     */
    fun cancelarSolicitud(solicitudId: Long, userId: Long): Int {
        val db = helper.writableDatabase
        var rowsAffected = 0

        val cv = ContentValues().apply {
            put("estado", "CANCELADA")
        }

        val whereClause = "id = ? AND user_id = ? AND estado = ?"
        val whereArgs = arrayOf(solicitudId.toString(), userId.toString(), "PENDIENTE")

        try {
            rowsAffected = db.update("solicitudes", cv, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e("Repo", "Error al cancelar solicitud: ${e.message}")
        } finally {
            db.close()
        }
        return rowsAffected
    }

    /**
     * Retrieves pending solicitudes for the specified zone ordered by creation time ascending.
     *
     * @param zona The zone name or identifier to filter solicitudes.
     * @return A list of `Solicitud` objects whose `estado` is `"PENDIENTE"` for the given zone, ordered by `createdAt` from oldest to newest. Returns an empty list if no matches are found or an error occurs.
     */
    fun pendientesPorZona(zona: String): List<Solicitud> {
        val db = helper.readableDatabase
        val items = mutableListOf<Solicitud>()
        var cursor: Cursor? = null

        val query = """
            SELECT * FROM solicitudes
            WHERE zona = ? AND estado = 'PENDIENTE'
            ORDER BY created_at ASC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, arrayOf(zona))
            if (cursor.moveToFirst()) {
                do {
                    items.add(cursorToSolicitud(cursor))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("Repo", "Error al listar solicitudes pendientes por zona: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return items
    }

    /**
     * Devuelve las solicitudes (SolicitudItem) en estados ASIGNADA, EN_CAMINO o EN RUTA para la zona indicada.
     *
     * @param zona Zona por la que se filtran las solicitudes.
     * @return Lista de SolicitudItem que coinciden con la zona y los estados ASIGNADA, EN_CAMINO o EN RUTA.
     */
    fun asignadasPorZona(zona: String): List<SolicitudItem> {
        val db = helper.readableDatabase
        val items = mutableListOf<SolicitudItem>()
        var cursor: Cursor? = null

        val query = """
            SELECT
                s.id,
                d.full_address,
                s.fecha,
                s.estado
            FROM
                solicitudes s
            INNER JOIN
                direcciones d ON s.direccion_id = d.id
            WHERE
                s.zona = ? AND s.estado IN ('ASIGNADA', 'EN_CAMINO', 'EN RUTA')
            ORDER BY
                s.fecha ASC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, arrayOf(zona))
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val addressIndex = cursor.getColumnIndexOrThrow("full_address")
                val dateIndex = cursor.getColumnIndexOrThrow("fecha")
                val statusIndex = cursor.getColumnIndexOrThrow("estado")

                do {
                    val item = SolicitudItem(
                        id = cursor.getLong(idIndex),
                        direccion = cursor.getString(addressIndex),
                        fecha = cursor.getString(dateIndex),
                        estado = cursor.getString(statusIndex)
                    )
                    items.add(item)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("Repo", "Error al listar solicitudes asignadas por zona: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return items
    }

    /**
     * Asigna una solicitud pendiente a un recolector/conductor específico.
     *
     * @param solicitudId ID de la solicitud a actualizar.
     * @param recolectorId ID del conductor o recolector a asignar.
     * @return Número de filas afectadas (1 si fue exitoso, 0 si falló o el estado no era 'PENDIENTE').
     */
    fun asignarRecolector(solicitudId: Long, recolectorId: Long): Int {
        val db = helper.writableDatabase
        var rowsAffected = 0

        val cv = ContentValues().apply {
            put("recolector_id", recolectorId)
            put("estado", "ASIGNADA")
        }

        // Solo se permite la asignación si la solicitud está en estado PENDIENTE
        val whereClause = "id = ? AND estado = 'PENDIENTE'"
        val whereArgs = arrayOf(solicitudId.toString())

        try {
            rowsAffected = db.update("solicitudes", cv, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e("Repo", "Error al asignar recolector: ${e.message}")
        } finally {
            db.close()
        }
        return rowsAffected
    }

    /**
     * Retrieve assigned requests for a specific collector/driver.
     *
     * @param recolectorId The ID of the collector/driver.
     * @return A list of `SolicitudItem` representing the requests assigned to the collector, excluding requests in states "ENTREGADA" or "CANCELADA", ordered by `fecha` ascending.
     */
    fun getSolicitudesByRecolectorId(recolectorId: Long): List<SolicitudItem> {
        val solicitudes = mutableListOf<SolicitudItem>()
        val db = helper.readableDatabase
        var cursor: Cursor? = null

        // Consulta SQL que une la tabla de solicitudes con la tabla de direcciones
        // y filtra por el ID del recolector.
        val query = "SELECT s.id, d.full_address, s.fecha, s.estado " +
                "FROM solicitudes s " +
                "INNER JOIN direcciones d ON s.direccion_id = d.id " +
                "WHERE s.recolector_id = ? AND s.estado NOT IN ('ENTREGADA', 'CANCELADA') " +
                "ORDER BY s.fecha ASC"

        try {
            cursor = db.rawQuery(query, arrayOf(recolectorId.toString()))
            if (cursor.moveToFirst()) {
                do {
                    // Leer datos del cursor
                    val id = cursor.getLong(0)
                    val direccion = cursor.getString(1)
                    val fecha = cursor.getString(2)
                    val estado = cursor.getString(3)

                    solicitudes.add(SolicitudItem(id, direccion, fecha, estado))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(
                "SolicitudRepository",
                "Error al obtener solicitudes por Recolector ID: ${e.message}"
            )
        } finally {
            cursor?.close()
            db.close()
        }
        return solicitudes
    }

    /**
     * Maps the current cursor row to a Solicitud instance.
     *
     * @param cursor A Cursor positioned at the row to map. Must contain columns: `id`, `user_id`, `direccion_id`,
     * `fecha`, `franja`, `notas`, `zona`, `estado`, and `created_at`. Optional columns that will be read if present:
     * `recolector_id`, `guia_id`, `confirmation_code`.
     * @return A Solicitud populated from the cursor row, with optional fields set to `null` when absent or null in the row.
     */
    private fun cursorToSolicitud(cursor: Cursor): Solicitud {
        val idIndex = cursor.getColumnIndexOrThrow("id")
        val userIdIndex = cursor.getColumnIndexOrThrow("user_id")
        val direccionIdIndex = cursor.getColumnIndexOrThrow("direccion_id")
        val fechaIndex = cursor.getColumnIndexOrThrow("fecha")
        val franjaIndex = cursor.getColumnIndexOrThrow("franja")
        val notasIndex = cursor.getColumnIndexOrThrow("notas")
        val zonaIndex = cursor.getColumnIndexOrThrow("zona")
        val estadoIndex = cursor.getColumnIndexOrThrow("estado")
        val createdAtIndex = cursor.getColumnIndexOrThrow("created_at")

        val recolectorIdIndex = cursor.getColumnIndex("recolector_id")
        val guiaIdIndex = cursor.getColumnIndex("guia_id")
        val confirmationCodeIndex = cursor.getColumnIndex("confirmation_code")

        return Solicitud(
            id = cursor.getLong(idIndex),
            userId = cursor.getLong(userIdIndex),
            direccionId = cursor.getLong(direccionIdIndex),
            fecha = cursor.getString(fechaIndex),
            franja = cursor.getString(franjaIndex),
            notas = cursor.getString(notasIndex),
            zona = cursor.getString(zonaIndex),
            estado = cursor.getString(estadoIndex),
            createdAt = cursor.getLong(createdAtIndex),

            recolectorId = if (recolectorIdIndex != -1 && !cursor.isNull(recolectorIdIndex)) cursor.getLong(
                recolectorIdIndex
            ) else null,
            guiaId = if (guiaIdIndex != -1 && !cursor.isNull(guiaIdIndex)) cursor.getLong(
                guiaIdIndex
            ) else null,
            confirmationCode = if (confirmationCodeIndex != -1 && !cursor.isNull(
                    confirmationCodeIndex
                )
            ) cursor.getString(confirmationCodeIndex) else null
        )
    }

}