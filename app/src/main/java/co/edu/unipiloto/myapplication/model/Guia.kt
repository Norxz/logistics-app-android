// co.edu.unipiloto.myapplication.model.Guia.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Representa la guía de envío con la información de rastreo, estado y costos.
 * Utilizada para mapear los datos recibidos desde el API REST.
 */
data class Guia(
    // Campos AÑADIDOS
    @SerializedName("id")
    val id: Long? = null,

    // Campos existentes
    @SerializedName("numeroGuia")
    val numeroGuia: String,

    @SerializedName("trackingNumber")
    val trackingNumber: String,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String? = null, // Mapeado de Instant a String (ISO 8601)

    // Campos AÑADIDOS
    @SerializedName("costoEnvio")
    val costoEnvio: Double? = null,

    @SerializedName("estadoGuia")
    val estadoGuia: String = "CREADA",

    @SerializedName("ultimaActualizacion")
    val ultimaActualizacion: String? = null // Mapeado de Instant a String (ISO 8601)
) : Serializable