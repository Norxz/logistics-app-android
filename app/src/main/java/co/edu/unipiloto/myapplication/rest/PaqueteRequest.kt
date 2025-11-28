// co.edu.unipiloto.myapplication.dto.PaqueteRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📦 DTO utilizado por el cliente (Android) para enviar las especificaciones
 * físicas del paquete al backend durante la creación de una solicitud.
 *
 * @property peso Peso del paquete en kilogramos (obligatorio).
 * @property alto Altura del paquete en centímetros (opcional).
 * @property ancho Ancho del paquete en centímetros (opcional).
 * @property largo Largo del paquete en centímetros (opcional).
 * @property contenido Descripción breve del contenido (opcional).
 */
data class PaqueteRequest(
    @SerializedName("peso")
    val peso: Double,

    @SerializedName("alto")
    val alto: Double?,

    @SerializedName("ancho")
    val ancho: Double?,

    @SerializedName("largo")
    val largo: Double?,

    @SerializedName("contenido")
    val contenido: String?
) : Serializable