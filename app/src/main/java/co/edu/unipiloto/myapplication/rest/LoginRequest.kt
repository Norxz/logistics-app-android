// co.edu.unipiloto.myapplication.dto.LoginRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 🔑 DTO utilizado por el cliente (Android) para enviar las credenciales
 * al endpoint de autenticación del backend.
 *
 * @property email Correo electrónico del usuario que intenta iniciar sesión.
 * @property password Contraseña del usuario en texto plano.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
) : Serializable