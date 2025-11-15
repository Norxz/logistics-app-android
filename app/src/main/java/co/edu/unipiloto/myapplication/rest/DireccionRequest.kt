package co.edu.unipiloto.myapplication.rest

data class DireccionRequest(
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double?,
    val longitud: Double?,
    val pisoApto: String?,
    val notasEntrega: String?
)
