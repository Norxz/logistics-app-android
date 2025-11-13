package co.edu.unipiloto.myapplication.rest

data class SolicitudRequest(
    // Datos de la Sesión (ID del cliente logueado)
    val clientId: Long,

    // Datos del Paquete y Costo
    val pesoKg: Double,
    val precio: Double,
    val notas: String?,

    // Datos de Recolección (Recibidos desde RecogidaActivity/SolicitudActivity)
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val pisoApto: String?,
    val notasEntrega: String?,

    // Datos de la Logística
    val zona: String,
    val fechaRecoleccion: String,
    val franjaHoraria: String
)