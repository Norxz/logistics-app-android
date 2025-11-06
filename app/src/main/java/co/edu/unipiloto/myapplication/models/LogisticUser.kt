package co.edu.unipiloto.myapplication.models

/**
 * Modelo de datos para representar a un usuario del personal logístico.
 */
data class LogisticUser(
    val id: Long,
    val email: String,
    val name: String,
    val role: String, // CONDUCTOR, FUNCIONARIO, ANALISTA, GESTOR
    val sucursal: String?, // Zona asignada
    val phoneNumber: String,
    val isActive: Boolean,
    // La contraseña no se almacena en el modelo de la UI por seguridad
)