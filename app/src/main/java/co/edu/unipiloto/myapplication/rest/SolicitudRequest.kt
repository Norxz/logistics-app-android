package co.edu.unipiloto.myapplication.rest

data class SolicitudRequest(
    val clientId: Long,
    val sucursalId: Long?,
    val remitente: ClienteRequest,
    val receptor: ClienteRequest,
    val direccion: DireccionRequest,
    val paquete: PaqueteRequest,
    val fechaRecoleccion: String,
    val franjaHoraria: String
)

