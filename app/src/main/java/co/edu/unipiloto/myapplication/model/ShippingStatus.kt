package co.edu.unipiloto.myapplication.model

/**
 * Modelo de datos para mostrar el estado de seguimiento (Tracking) en la UI.
 * Este modelo refleja los datos que DEBE retornar el endpoint de Tracking del backend.
 */
data class ShippingStatus(
    // 1. Identificador de Guía (Usaremos el tracking number real del backend)
    // El backend podría devolver el número de guía completo.
    val trackingNumber: String,

    // 2. Estado (PENDIENTE, EN RUTA, ENTREGADO)
    val status: String,

    // 3. Dirección de Destino Completa
    val destinationAddress: String,

    // 4. Fecha y Franja de Entrega (Campos generados por tu antigua lógica, útiles para la UI)
    val estimatedDate: String,
    val timeFranja: String
) {
}