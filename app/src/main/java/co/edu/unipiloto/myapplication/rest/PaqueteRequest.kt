package co.edu.unipiloto.myapplication.rest

data class PaqueteRequest(
    val peso: Double,
    val alto: Double?,
    val ancho: Double?,
    val largo: Double?,
    val contenido: String?
)