package co.edu.unipiloto.myapplication.rest

data class LoginRequest(
    val email: String,
    val password: String // Contraseña en texto plano
)