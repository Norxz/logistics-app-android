package co.edu.unipiloto.myapplication.dto

/**
 * DTO para la Dirección.
 * Debe coincidir con la estructura de la entidad Direccion en el backend,
 * pero solo con los campos necesarios.
 */
data class DireccionResponse(
    val id: Long,
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val pisoApto: String?, // Opcional
    val notasEntrega: String? // Opcional
)