// co.edu.unipiloto.myapplication.model.Direccion.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Representa una dirección física utilizada en Solicitudes y Sucursales,
 * mapeando los datos recibidos del API REST.
 */
data class Direccion(
    // Campos existentes
    @SerializedName("id")
    val id: Long? = null,

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

    // Campos AÑADIDOS para coincidir con el backend
    @SerializedName("barrio")
    val barrio: String? = null,

    @SerializedName("codigoPostal")
    val codigoPostal: String? = null,

    @SerializedName("tipoDireccion")
    val tipoDireccion: String? = null
) : Serializable