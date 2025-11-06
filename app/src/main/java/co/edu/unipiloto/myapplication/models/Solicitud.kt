package co.edu.unipiloto.myapplication.models

/**
 * Modelo de datos para representar una Solicitud de Recolección.
 * Este objeto combina datos de varias tablas (solicitudes, guia, direcciones)
 * para una representación completa en la UI.
 */
data class Solicitud(
    // --- Datos de la tabla 'solicitudes' ---
    val id: Long,
    val userId: Long,
    val recolectorId: Long?, // Nullable: aún no asignado
    val direccionId: Long,
    val fecha: String, // Formato "YYYY-MM-DD"
    val franja: String, // Ej: "8:00-12:00", "12:00-16:00"
    val notas: String?,
    val zona: String,
    val guiaId: Long?,
    val estado: String, // Ej: "PENDIENTE", "ASIGNADA", "RECOGIDA", "FINALIZADA"
    val confirmationCode: String?,
    val createdAt: Long, // Timestamp

    // --- Datos de la tabla 'direcciones' (Denormalizados para la UI) ---
    val fullAddress: String,
    val ciudad: String,

    // --- Datos de la tabla 'guia' (Denormalizados para la UI) ---
    val trackingNumber: String?, // Nullable: solo si ya se generó la guía
    val descripcion: String?,
    val valor: Double,
    val peso: Double,

    // --- Datos Auxiliares (Opcionales para la UI) ---
    // Nombre del cliente que realiza la solicitud (útil para el conductor/gestor)
    val clientName: String? = null
)