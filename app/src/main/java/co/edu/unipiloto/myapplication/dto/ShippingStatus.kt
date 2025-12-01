package co.edu.unipiloto.myapplication.dto

import java.io.Serializable

/**
 * 🚚 Data Transfer Object (DTO) que representa el estado actual de un envío
 * obtenido del servicio de rastreo (tracking).
 *
 * Esta clase se utiliza para mapear la respuesta JSON de la API REST del backend
 * en la función de búsqueda de envíos.
 *
 * @property status El estado actual del envío (ej: "EN CAMINO", "ENTREGADO", "EN BODEGA").
 * @property trackingNumber El número de identificación o código de guía del envío.
 * @property destinationAddress La dirección completa de destino final del paquete.
 * @property estimatedDate La fecha estimada de entrega del envío (formato string, ej: "YYYY-MM-DD").
 * @property timeFranja La franja horaria estimada para la entrega (ej: "AM", "PM", "TODO EL DÍA").
 */
data class ShippingStatus(
    val status: String,
    val trackingNumber: String,
    val destinationAddress: String,
    val estimatedDate: String,
    val timeFranja: String
) : Serializable