// co.edu.unipiloto.myapplication.rest.GuiaResponse.kt
package co.edu.unipiloto.myapplication.rest

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * DTO (Data Transfer Object) utilizado para recibir y representar la información
 * esencial de una Guía de Envío desde el API REST.
 *
 * @property numeroGuia Número identificador único de la guía.
 * @property trackingNumber Código de rastreo que permite al cliente seguir el envío.
 * @property fechaCreacion Fecha y hora de creación de la guía (formato ISO 8601 String).
 */
data class GuiaResponse(
    @SerializedName("numeroGuia")
    val numeroGuia: String,

    @SerializedName("trackingNumber")
    val trackingNumber: String,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String // Mapea el Instant del backend como un String
) : Serializable