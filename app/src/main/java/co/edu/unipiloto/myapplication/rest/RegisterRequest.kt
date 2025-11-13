package co.edu.unipiloto.myapplication.rest

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String, // Recibimos la contraseña en texto plano (temporalmente)
    val phoneNumber: String?,
    val role: String,
    val sucursal: String?,
    val isActive: Boolean = true
)