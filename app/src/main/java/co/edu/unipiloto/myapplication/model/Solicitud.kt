
package co.edu.unipiloto.myapplication.model

import java.io.Serializable
/**
 * Modelo de datos para representar una Solicitud de Recolección o Entrega.
 * Esta es la entidad central para la gestión administrativa.
 *
 * @property id ID de la tabla SOLICITUDES.
 * @property guiaId El número de guía o referencia.
 * @property type El tipo de servicio (ej. 'RECOLECCIÓN', 'ENTREGA').
 * @property status El estado actual ('PENDIENTE', 'ASIGNADA', 'EN_RUTA', 'COMPLETADA', 'CANCELADA').
 * @property address Dirección completa de la solicitud.
 * @property clientName Nombre del cliente solicitante.
 * @property clientPhone Número de teléfono del cliente.
 * @property creationTimestamp Fecha y hora de la creación de la solicitud.
 * @property assignedRecolectorId ID de la tabla RECOLECTORES asignado (FK). Puede ser nulo.
 * @property assignedRecolectorName Nombre del recolector asignado (para mostrar en la UI).
 */
data class Solicitud(
    val id: Long,
    val guiaId: String,
    val type: String,
    val status: String,
    val address: String,
    val clientName: String,
    val clientPhone: String?, // Puede ser opcional
    val creationTimestamp: String,
    val assignedRecolectorId: Long? = null,
    val assignedRecolectorName: String? = null
):Serializable