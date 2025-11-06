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
    // ==========================================================

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
     * Obtiene una lista de solicitudes simplificadas ([SolicitudItem]) que están en estado
     * ASIGNADA, EN_CAMINO o EN RUTA, filtradas por zona.
     *
     * Este método es usado por el Gestor/Funcionario para ver las órdenes en curso.
     *
     * @param zona La zona para la cual se buscan solicitudes asignadas.
     * @return Una lista de objetos SolicitudItem.
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
     * Obtiene todas las solicitudes asignadas a un recolector/conductor específico.
     *
     * @param recolectorId El ID del recolector/conductor.
     * @return Lista de SolicitudItem (las rutas asignadas).
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

    // ... [Método cursorToSolicitud, si no estaba al final] ...
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