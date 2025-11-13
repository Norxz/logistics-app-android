package co.edu.unipiloto.myapplication.rest


data class GuiaResponse(
    val numeroGuia: String,
    val trackingNumber: String,
    val fechaCreacion: String // Simplificado a String
) {}