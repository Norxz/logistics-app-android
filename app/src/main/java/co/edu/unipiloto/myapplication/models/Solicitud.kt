package co.edu.unipiloto.myapplication.models

import java.io.Serializable // Recommended for models used in intents


/**
 * Modelo DTO utilizado por la UI, mapeado de la respuesta del Backend.
 * Coincide con la estructura de co.edu.unipiloto.backend.model.Solicitud.
 */
data class Solicitud(
    // METADATA & RELATIONS (Needed by Adapter)
    val id: Long, // 👈 Required by adapter (previously missing)
    val estado: String, // 👈 Required by adapter (previously missing)
    val createdAt: String, // 👈 Required by adapter (previously missing, usually Instant or String)
    val guia: Guia, // 👈 Required for tracking number (previously missing)
    val direccion: Direccion, // 👈 Required for address details (previously missing/incorrect)

    // Remitente
    val remitenteNombre: String, // Adapter uses 'remitente' (must be mapped)
    val remitenteTipoId: String,
    val remitenteNumeroId: String,
    val remitenteTelefono: String,
    val remitenteCodigoPais: String,

    // Paquete
    val alto: Double?,
    val ancho: Double?,
    val largo: Double?,
    val pesoKg: Double,
    val contenido: String?,

    // Destinatario (Receptor)
    val receptorNombre: String, // Adapter uses 'nombreReceptor' (must be mapped)
    val receptorTipoId: String,
    val receptorNumeroId: String,
    val receptorTelefono: String,
    val receptorCodigoPais: String,
    // Note: direccionReceptor and ciudad from your old model are now inside 'direccion' object

    // Logística
    val fechaRecoleccion: String,
    val franjaHoraria: String,
    val precio: Double
) : Serializable // Recommended to implement Serializable