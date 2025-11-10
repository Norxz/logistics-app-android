package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
// 🏆 IMPORTAR el modelo de datos ENRIQUECIDO
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.db.SolicitudRepository.Companion.TABLE_SOLICITUDES
import java.util.UUID // Necesario para generar un tracking_number único

/**
 * Repositorio encargado de gestionar las operaciones CRUD (Crear, Leer, Actualizar, Borrar)
 * para las solicitudes de envío, así como sus dependencias (direcciones y guías).
 */
class SolicitudRepository(context: Context) {

    private val helper: DBHelper = DBHelper(context)
    private val TAG = "SolicitudRepo"

    companion object {
        // Constantes para los nombres de las tablas para mejorar la legibilidad del SQL
        private const val TABLE_SOLICITUDES = "solicitudes"
        private const val TABLE_DIRECCIONES = "direcciones"
        private const val TABLE_USERS = "users"
        private const val TABLE_GUIA = "guia"
    }

    // ==========================================================
    // 1. CREACIÓN (Cliente)
    // ==========================================================

    /**
     * Crea una nueva solicitud de recolección, insertando la dirección, la guía (paquete) y la solicitud en una transacción.
     *
     * @return El ID de la nueva solicitud insertada, o -1L si la transacción falla.
     */
    fun crearSolicitud(
        userId: Long,
        direccionCompleta: String,
        ciudad: String,
        latitudRecoleccion: Double,
        longitudRecoleccion: Double,
        peso: Double,
        precio: Double,
        fechaRecoleccion: String,
        franjaHoraria: String,
        notas: String?,
        zona: String
    ): Long {
        val db = helper.writableDatabase
        var newSolicitudId: Long = -1L
        var newDireccionId: Long = -1L
        var newGuiaId: Long = -1L // Nuevo ID de Guía

        db.beginTransaction()
        try {
            // 1. CREAR LA DIRECCIÓN ASOCIADA
            val cvDireccion = ContentValues().apply {
                put("user_id", userId)
                put("ciudad", ciudad)
                put("full_address", direccionCompleta)
                put("created_at", System.currentTimeMillis())
                put("latitude", latitudRecoleccion) // <-- INSERTAR LATITUD
                put("longitude", longitudRecoleccion)
            }
            newDireccionId = db.insert(TABLE_DIRECCIONES, null, cvDireccion)

            if (newDireccionId == -1L) throw Exception("Error al insertar la dirección.")

            // 2. CREAR LA GUÍA (PAQUETE)
            val trackingNumber = UUID.randomUUID().toString().substring(0, 10).uppercase()
            val cvGuia = ContentValues().apply {
                put("tracking_number", trackingNumber)
                put("descripcion", notas) // Usamos las notas como descripción del contenido
                put("valor", precio)
                put("peso", peso)
                put("created_at", System.currentTimeMillis())
            }
            newGuiaId = db.insert(TABLE_GUIA, null, cvGuia)

            if (newGuiaId == -1L) throw Exception("Error al insertar la guía.")


            // 3. CREAR LA SOLICITUD
            val cvSolicitud = ContentValues().apply {
                put("user_id", userId)
                put("direccion_id", newDireccionId)
                put("guia_id", newGuiaId) // <-- ASOCIAR EL ID DE LA GUÍA
                put("fecha", fechaRecoleccion)
                put("franja", franjaHoraria)
                put("notas", notas)
                put("zona", zona)
                put("estado", "PENDIENTE")
                put("created_at", System.currentTimeMillis())
            }
            newSolicitudId = db.insert(TABLE_SOLICITUDES, null, cvSolicitud)

            if (newSolicitudId == -1L) throw Exception("Error al insertar la solicitud.")

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Transacción fallida al crear solicitud: ${e.message}")
            newSolicitudId = -1L
        } finally {
            db.endTransaction()
            db.close()
        }
        return newSolicitudId
    }

    // ==========================================================
    // 2. LECTURA (Listados Enriquecidos para Adaptador)
    // ==========================================================

    // 🏆 CLASE DE DATOS REQUERIDA POR EL ADAPTADOR (Agregada para que SolicitudAdapter funcione)
    data class SolicitudItem(
        val id: Long,
        val estado: String,
        val direccion: String, // Usado para tvAddress
        val fecha: String,    // Usado para tvDate
        val zona: String,
        val franjaHoraria: String,
        val idCliente: Long
    )

    // ... (El resto de tu código de lectura, actualización y mapeo es correcto y se mantiene) ...

    /**
     * Obtiene una lista ENRIQUECIDA de solicitudes por ID de Usuario (Cliente).
     * El resultado se mapea al modelo completo [Solicitud] para el adaptador.
     */
    fun getSolicitudesEnrichedByUserId(userId: Long): List<Solicitud> {
        return executeEnrichedQuery(
            whereClause = "s.user_id = ?",
            whereArgs = arrayOf(userId.toString())
        )
    }

    /**
     * Obtiene una lista ENRIQUECIDA de solicitudes asignadas a un recolector/conductor.
     *
     * @param recolectorId El ID del recolector/conductor.
     * @return Lista de objetos [Solicitud] para el adaptador.
     */
    fun getSolicitudesEnrichedByRecolectorId(recolectorId: Long): List<Solicitud> {
        return executeEnrichedQuery(
            // El conductor solo ve las rutas que no han sido finalizadas o canceladas.
            whereClause = "s.recolector_id = ? AND s.estado NOT IN ('ENTREGADA', 'CANCELADA', 'FINALIZADA')",
            whereArgs = arrayOf(recolectorId.toString())
        )
    }

    /**
     * Obtiene una lista ENRIQUECIDA de solicitudes ASIGNADAS o en ruta, filtradas por ZONA.
     * Este método es usado por el Gestor/Funcionario para ver las órdenes en curso.
     *
     * @param zona La zona para la cual se buscan solicitudes asignadas.
     * @return Lista de objetos [Solicitud] enriquecidos.
     */
    fun getSolicitudesAsignadasEnriquecidasPorZona(zona: String): List<Solicitud> {
        return executeEnrichedQuery(
            whereClause = "s.zona = ? AND s.estado IN ('ASIGNADA', 'EN_RECOLECCION', 'RECOGIDA')",
            whereArgs = arrayOf(zona)
        )
    }

    /**
     * Obtiene una lista ENRIQUECIDA de solicitudes PENDIENTES, filtradas por ZONA.
     *
     * @param zona La zona para la cual se buscan solicitudes pendientes de asignación.
     * @return Lista de objetos [Solicitud] enriquecidos.
     */
    fun getSolicitudesPendientesEnriquecidasPorZona(zona: String): List<Solicitud> {
        return executeEnrichedQuery(
            whereClause = "s.zona = ? AND s.estado = 'PENDIENTE'",
            whereArgs = arrayOf(zona)
        )
    }

    /**
     * Obtiene una lista ENRIQUECIDA de solicitudes FINALIZADAS (ENTREGADA, CANCELADA, FINALIZADA)
     * filtradas por ZONA. Usado para el historial del Gestor/Funcionario.
     *
     * @param zona La zona para la cual se buscan solicitudes completadas.
     * @return Lista de objetos [Solicitud] enriquecidos.
     */
    fun getSolicitudesFinalizadasEnriquecidasPorZona(zona: String): List<Solicitud> {
        return executeEnrichedQuery(
            whereClause = "s.zona = ? AND s.estado IN ('ENTREGADA', 'CANCELADA', 'FINALIZADA')",
            whereArgs = arrayOf(zona)
        )
    }


    /**
     * Implementación centralizada de la consulta JOIN para obtener el modelo [Solicitud] completo.
     */
    private fun executeEnrichedQuery(
        whereClause: String,
        whereArgs: Array<String>
    ): List<Solicitud> {
        val solicitudes = mutableListOf<Solicitud>()
        val db = helper.readableDatabase
        var cursor: Cursor? = null

        val query = """
            SELECT 
                s.id, s.user_id, s.recolector_id, s.direccion_id, s.fecha, s.franja, s.notas, s.zona, s.guia_id, s.estado, s.confirmation_code, s.created_at,
                d.full_address, d.ciudad,
                u.name AS client_name,
                g.tracking_number, g.descripcion, g.valor, g.peso
            FROM 
                $TABLE_SOLICITUDES s
            INNER JOIN 
                $TABLE_DIRECCIONES d ON s.direccion_id = d.id
            INNER JOIN
                $TABLE_USERS u ON s.user_id = u.id
            LEFT JOIN
                $TABLE_GUIA g ON s.guia_id = g.id
            WHERE 
                $whereClause
            ORDER BY 
                s.created_at DESC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, whereArgs)
            while (cursor.moveToNext()) {
                solicitudes.add(cursorToSolicitudEnriquecida(cursor))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al ejecutar consulta enriquecida: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return solicitudes
    }

    // ==========================================================
    // 3. ACTUALIZACIÓN (Gestor/Conductor)
    // ==========================================================

    /**
     * Actualiza el estado de una solicitud.
     */
    fun actualizarEstado(solicitudId: Long, newState: String, recolectorId: Long? = null): Int {
        val db = helper.writableDatabase
        var rowsAffected = 0

        val cv = ContentValues().apply {
            put("estado", newState)
        }

        // El conductor solo puede actualizar su propia ruta, el gestor/admin puede actualizar cualquiera.
        val whereClause = if (recolectorId != null) {
            "id = ? AND recolector_id = ?"
        } else {
            "id = ?"
        }

        val whereArgs = if (recolectorId != null) {
            arrayOf(solicitudId.toString(), recolectorId.toString())
        } else {
            arrayOf(solicitudId.toString())
        }


        try {
            rowsAffected = db.update(TABLE_SOLICITUDES, cv, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado: ${e.message}")
        } finally {
            db.close()
        }
        return rowsAffected
    }

    /**
     * Asigna una solicitud pendiente a un recolector/conductor específico.
     */
    fun asignarRecolector(solicitudId: Long, recolectorId: Long): Int {
        val db = helper.writableDatabase
        var rowsAffected = 0

        val cv = ContentValues().apply {
            put("recolector_id", recolectorId)
            put("estado", "ASIGNADA")
        }

        val whereClause = "id = ? AND estado = 'PENDIENTE'"
        val whereArgs = arrayOf(solicitudId.toString())

        try {
            rowsAffected = db.update(TABLE_SOLICITUDES, cv, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "Error al asignar recolector: ${e.message}")
        } finally {
            db.close()
        }
        return rowsAffected
    }

    // ==========================================================
    // 4. MAPPING (Cursor a Modelo de Dominio)
    // ==========================================================

    /**
     * Mapea un [Cursor] de la consulta ENRIQUECIDA a un objeto [Solicitud] completo.
     */
    private fun cursorToSolicitudEnriquecida(cursor: Cursor): Solicitud {

        // Columnas de la tabla SOLICITUDES
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
        val recolectorIdIndex = cursor.getColumnIndexOrThrow("recolector_id")
        val recolectorId =
            if (!cursor.isNull(recolectorIdIndex)) cursor.getLong(recolectorIdIndex) else null
        val direccionId = cursor.getLong(cursor.getColumnIndexOrThrow("direccion_id"))
        val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
        val franja = cursor.getString(cursor.getColumnIndexOrThrow("franja"))
        val notas = cursor.getString(cursor.getColumnIndexOrThrow("notas"))
        val zona = cursor.getString(cursor.getColumnIndexOrThrow("zona"))
        val guiaIdIndex = cursor.getColumnIndexOrThrow("guia_id")
        val guiaId = if (!cursor.isNull(guiaIdIndex)) cursor.getLong(guiaIdIndex) else null
        val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
        val confirmationCodeIndex = cursor.getColumnIndexOrThrow("confirmation_code")
        val confirmationCode =
            if (!cursor.isNull(confirmationCodeIndex)) cursor.getString(confirmationCodeIndex) else null
        val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))

        // Columnas de la tabla DIRECCIONES
        val fullAddress = cursor.getString(cursor.getColumnIndexOrThrow("full_address"))
        val ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ciudad"))

        // Columnas de la tabla USERS
        val clientNameIndex = cursor.getColumnIndexOrThrow("client_name")
        val clientName =
            if (!cursor.isNull(clientNameIndex)) cursor.getString(clientNameIndex) else "N/D"

        // Columnas de la tabla GUIA (Nullable)
        val trackingNumberIndex = cursor.getColumnIndexOrThrow("tracking_number")
        val trackingNumber =
            if (!cursor.isNull(trackingNumberIndex)) cursor.getString(trackingNumberIndex) else null
        val descripcionIndex = cursor.getColumnIndexOrThrow("descripcion")
        val descripcion =
            if (!cursor.isNull(descripcionIndex)) cursor.getString(descripcionIndex) else null
        val valorIndex = cursor.getColumnIndexOrThrow("valor")
        val valor = if (!cursor.isNull(valorIndex)) cursor.getDouble(valorIndex) else 0.0
        val pesoIndex = cursor.getColumnIndexOrThrow("peso")
        val peso = if (!cursor.isNull(pesoIndex)) cursor.getDouble(pesoIndex) else 0.0

        return Solicitud(
            id = id,
            userId = userId,
            recolectorId = recolectorId,
            direccionId = direccionId,
            fecha = fecha,
            franja = franja,
            notas = notas,
            zona = zona,
            guiaId = guiaId,
            estado = estado,
            confirmationCode = confirmationCode,
            createdAt = createdAt,
            fullAddress = fullAddress,
            ciudad = ciudad,
            clientName = clientName,
            trackingNumber = trackingNumber,
            descripcion = descripcion,
            valor = valor,
            peso = peso
        )
    }

}