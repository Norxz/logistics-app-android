// co.edu.unipiloto.myapplication.model.User.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Representa un usuario del sistema para el frontend (excluye el passwordHash).
 */
data class User(
    @SerializedName("id")
    val id: Long? = null, // Usar Long? para seguridad

    @SerializedName("documento")
    val documento: String? = null, // Campo añadido

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("role")
    val role: String, // Mapeado del Enum Role a String

    @SerializedName("sucursal")
    val sucursal: Sucursal? = null, // Relación con el modelo Sucursal

    @SerializedName("fechaCreacion")
    val fechaCreacion: String? = null, // Instant serializado a String

    @SerializedName("ultimoLogin")
    val ultimoLogin: String? = null, // Instant serializado a String

    @SerializedName("isActive")
    val isActive: Boolean = true
) : Serializable