// co.edu.unipiloto.myapplication.dto.DireccionRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * DTO para enviar la información de una Dirección al backend,
 * usado para Solicitudes y Sucursales.
 */
data class DireccionRequest(
    @SerializedName("direccionCompleta")
    val direccionCompleta: String,

    @SerializedName("ciudad")
    val ciudad: String,

    @SerializedName("latitud")
    val latitud: Double?,

    @SerializedName("longitud")
    val longitud: Double?,

    @SerializedName("pisoApto")
    val pisoApto: String?,

    @SerializedName("notasEntrega")
    val notasEntrega: String?,

    // Campos AÑADIDOS para coincidir con el DTO del backend
    @SerializedName("barrio")
    val barrio: String? = null,

    @SerializedName("codigoPostal")
    val codigoPostal: String? = null,

    @SerializedName("tipoDireccion")
    val tipoDireccion: String? = null
) : Serializable