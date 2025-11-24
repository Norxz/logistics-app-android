package co.edu.unipiloto.myapplication.model

/**
 * DTO utilizado para enviar datos del usuario al cliente (Android/Postman)
 * después de un login/registro exitoso, omitiendo el passwordHash.
 */
data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val role: String,
    val sucursal: Sucursal?,
    val isActive: Boolean
) {

}