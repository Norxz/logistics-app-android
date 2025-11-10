package co.edu.unipiloto.myapplication.models

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val role: String,
    val sucursal: String?,
    val isActive: Boolean = true
)