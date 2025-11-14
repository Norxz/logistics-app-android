package co.edu.unipiloto.myapplication.rest

data class SolicitudRequest(
    val clientId: Long,

    // -- Remitente --
    val remitenteNombre: String,
    val remitenteTipoId: String,
    val remitenteNumeroId: String,
    val remitenteTelefono: String?,
    val remitenteCodigoPais: String?,

    // -- Paquete --
    val alto: Double?,
    val ancho: Double?,
    val largo: Double?,
    val pesoKg: Double, // Non-nullable
    val contenido: String?,

    // -- Receptor --
    val receptorNombre: String,
    val receptorTipoId: String,
    val receptorNumeroId: String,
    val receptorTelefono: String?,
    val receptorCodigoPais: String?,

    // -- Direccion de recolección --
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val pisoApto: String? = null,
    val notasEntrega: String? = null,

    // -- Logística --
    val zona: String,
    val fechaRecoleccion: String,
    val franjaHoraria: String,
    val precio: Double // Non-nullable
)