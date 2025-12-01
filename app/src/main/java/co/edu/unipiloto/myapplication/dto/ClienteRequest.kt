// co.edu.unipiloto.myapplication.dto.ClienteRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * DTO para enviar información de un Cliente (Remitente o Receptor)
 * al backend, usado para creación o actualización.
 */
data class ClienteRequest(
    // Campo añadido: Necesario para la identificación/actualización si el cliente ya existe
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("tipoId")
    val tipoId: String?,

    @SerializedName("numeroId")
    val numeroId: String?,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("codigoPais")
    val codigoPais: String?
) : Serializable