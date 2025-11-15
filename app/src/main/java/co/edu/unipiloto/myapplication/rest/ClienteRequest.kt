package co.edu.unipiloto.myapplication.rest

data class ClienteRequest(
    val nombre: String,
    val tipoId: String?,
    val numeroId: String?,
    val telefono: String?,
    val codigoPais: String?
)
