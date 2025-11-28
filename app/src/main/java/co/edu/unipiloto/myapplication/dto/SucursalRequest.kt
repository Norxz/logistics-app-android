// co.edu.unipiloto.myapplication.dto.SucursalRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 🏢 DTO utilizado por el cliente (Android) para enviar la información de una
 * Sucursal para su creación o actualización.
 *
 * @property nombre Nombre descriptivo de la sucursal.
 * @property direccion Objeto DTO que contiene la información completa de la dirección física.
 */
data class SucursalRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("direccion")
    val direccion: DireccionRequest
) : Serializable