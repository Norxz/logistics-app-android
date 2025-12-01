// co.edu.unipiloto.myapplication.dto.RegisterRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📝 DTO utilizado por el cliente (Android) para enviar los datos de un nuevo usuario
 * al endpoint de registro del backend.
 *
 * @property fullName Nombre completo del nuevo usuario.
 * @property email Correo electrónico (debe ser único).
 * @property password Contraseña en texto plano (será hasheada por el backend).
 * @property phoneNumber Número de teléfono (opcional).
 * @property role Rol asignado (ej. CLIENTE, CONDUCTOR).
 * @property sucursalId ID de la sucursal asignada (opcional, para roles operativos).
 * @property isActive Estado inicial del usuario (por defecto true).
 */
data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("role")
    val role: String,

    @SerializedName("sucursalId")
    val sucursalId: Long? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true
) : Serializable