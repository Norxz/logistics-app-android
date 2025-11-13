package co.edu.unipiloto.myapplication.models

import co.edu.unipiloto.myapplication.rest.GuiaResponse

// Modelo Anidado necesario para que Retrofit mapee la dirección anidada
data class DireccionResponse(
    val direccionCompleta: String,
    val ciudad: String
)

/**
 * Modelo DTO utilizado por la UI y el SolicitudAdapter.
 * Refleja la respuesta JSON de la Entidad Solicitud del Backend.
 */
data class Solicitud(
    val id: Long,
    val clientId: Long, // 👈 Relación al cliente (mapea client.id)

    // --- Campos de Logística (Directamente en la tabla 'solicitudes' del backend) ---
    val estado: String,
    val fechaRecoleccion: String, // Usar String para fecha (campo plano del backend)
    val franjaHoraria: String,
    val zona: String, // 👈 Campo necesario para filtrar por zona
    val pesoKg: Double, // 👈 Campo del paquete
    val precio: Double, // 👈 Campo del precio

    // El 'createdAt' es un String ISO 8601 que necesitas formatear en el Adapter
    val createdAt: String,

    // --- Estructuras Anidadas (Mapean a las Entidades relacionadas del backend) ---
    val direccion: DireccionResponse?,
    val guia: GuiaResponse?
)