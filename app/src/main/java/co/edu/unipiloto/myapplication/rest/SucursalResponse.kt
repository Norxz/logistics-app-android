// co.edu.unipiloto.myapplication.dto.SucursalResponse.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 🏢 DTO de respuesta simplificado utilizado por el backend para enviar
 * la información esencial de una Sucursal (ID y Nombre) al cliente (ej. en listas).
 *
 * @property id Identificador único de la sucursal.
 * @property nombre Nombre de la sucursal.
 */
data class SucursalResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("nombre")
    val nombre: String
) : Serializable