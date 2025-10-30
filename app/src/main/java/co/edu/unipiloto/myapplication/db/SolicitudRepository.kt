package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Repositorio encargado de gestionar las operaciones CRUD (Crear, Leer, Actualizar, Borrar) para las solicitudes de envío.
 *
 * Esta clase abstrae toda la lógica de acceso a la base de datos para la entidad `solicitudes`
 * y sus entidades relacionadas (como `direcciones`). Proporciona una API limpia y segura para que el resto
 * de la aplicación interactúe con los datos de las solicitudes sin conocer los detalles de la implementación de SQL.
 *
 * @property helper Una instancia de [DBHelper] para obtener acceso de lectura/escritura a la base de datos.
 * @constructor Crea una instancia del repositorio.
 * @param context El contexto de la aplicación, necesario para inicializar [DBHelper].
 */
class SolicitudRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)

    // ==========================================================
    // DATA CLASSES: Representación de las tablas/ítems
    // ==========================================================

    /**
     * Representa una solicitud de envío completa con todos sus campos, mapeando directamente
     * la estructura de la tabla `solicitudes` en la base de datos. Se utiliza cuando se necesita
     * acceso a todos los detalles de una solicitud.
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
     * Representa una vista simplificada de una solicitud, diseñada para ser mostrada en listas
     * o resúmenes de la UI donde no se necesita toda la información. Es ideal para optimizar el rendimiento
     * al consultar solo los datos estrictamente necesarios, reduciendo la carga de la base de datos.
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

    /**
     * Crea una nueva solicitud de envío en la base de datos.
     *
     * Esta operación es transaccional: inserta primero una dirección en la tabla `direcciones`
     * y luego la solicitud en la tabla `solicitudes`. Si alguna de las dos operaciones falla,
     * ninguna se consolida (rollback), manteniendo así la integridad de los datos.
     *
     * @param userId El ID del usuario que crea la solicitud.
     * @param direccionCompleta La dirección completa donde se debe recoger el paquete.
     * @param fechaRecoleccion La fecha deseada por el usuario para la recogida.
     * @param franjaHoraria La franja horaria preferida para la recogida (ej. "Mañana", "Tarde").
     * @param notas Instrucciones o comentarios adicionales para el recolector.
     * @param zona La zona geográfica de la recogida, usada para asignación logística.
     * @return El ID de la nueva solicitud creada, o -1L si la transacción falla.
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

            db.setTransactionSuccessful() // Marcar la transacción como exitosa
        } catch (e: Exception) {
            Log.e("Repo", "Transacción fallida al crear solicitud: ${e.message}")
            newSolicitudId = -1L // En caso de error, asegurar que se devuelva -1
        } finally {
            db.endTransaction() // Finalizar la transacción (commit si fue exitosa, rollback si no)
            db.close()
        }
        return newSolicitudId
    }

    /**
     * Obtiene una lista de solicitudes simplificadas ([SolicitudItem]) para un usuario específico.
     *
     * @param userId El ID del usuario cuyas solicitudes se quieren obtener.
     * @return Una lista de [SolicitudItem], o `null` si ocurre un error durante la consulta.
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
     * Cambia el estado de una solicitud a "CANCELADA".
     *
     * Esta operación es segura: solo permite la cancelación si la solicitud pertenece al `userId` proporcionado
     * y si su estado actual es "PENDIENTE". Esto previene que un usuario cancele solicitudes de otro
     * o que cancele una solicitud que ya está en proceso de recogida.
     *
     * @param solicitudId El ID de la solicitud a cancelar.
     * @param userId El ID del usuario que realiza la operación, para validación de propiedad.
     * @return El número de filas afectadas (debería ser 1 en caso de éxito, 0 si no se cumplieron las condiciones).
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
     * Obtiene una lista de solicitudes completas ([Solicitud]) que están en estado "PENDIENTE"
     * para una zona geográfica específica.
     *
     * @param zona La zona para la cual se buscan solicitudes pendientes.
     * @return Una lista de objetos [Solicitud]. La lista estará vacía si no hay solicitudes pendientes en esa zona.
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
     * Función auxiliar para mapear una fila de un [Cursor] a un objeto [Solicitud].
     *
     * Realiza un manejo seguro de las columnas que pueden ser nulas (como `recolector_id`)
     * para evitar excepciones [NullPointerException] al leer de la base de datos, usando `getColumnIndex`
     * y `isNull` para las columnas opcionales.
     *
     * @param cursor El cursor posicionado en la fila que se desea convertir.
     * @return Un objeto [Solicitud] poblado con los datos del cursor.
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

        // Obtener índices de columnas que PUEDEN ser NULL (No usar getColumnIndexOrThrow aquí)
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

            // MANEJO SEGURO DE NULOS
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

                    // Asumiendo que tienes una data class SolicitudItem definida para esto
                    solicitudes.add(SolicitudItem(id, direccion, fecha, estado))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            // Manejo de error de base de datos o consulta
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
}
