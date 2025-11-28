// co.edu.unipiloto.myapplication.dto.UserResponse.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 👤 DTO utilizado por el cliente (Android) para recibir la información del usuario
 * después de un proceso de login o registro exitoso. Excluye la contraseña y usa
 * el DTO simplificado para la sucursal.
 *
 * @property id Identificador único del usuario.
 * @property fullName Nombre completo del usuario.
 * @property email Correo electrónico.
 * @property phoneNumber Número de teléfono (opcional).
 * @property role Rol del usuario (String, ej: "CLIENTE").
 * @property sucursal Información simplificada de la sucursal asignada (opcional).
 * @property isActive Indica si la cuenta del usuario está activa.
 */
data class UserResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("sucursal")
    val sucursal: SucursalResponse?,

    @SerializedName("isActive")
    val isActive: Boolean
) : Serializable