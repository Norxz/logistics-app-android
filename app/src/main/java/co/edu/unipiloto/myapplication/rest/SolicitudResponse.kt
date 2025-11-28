// co.edu.unipiloto.myapplication.dto.SolicitudResponse.kt
package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.rest.GuiaResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📊 DTO de respuesta simplificado utilizado para mostrar un resumen de una Solicitud
 * en listas o tablas de la interfaz de usuario (UI), sin cargar todas las relaciones.
 *
 * @property id ID de la solicitud.
 * @property clientId ID del usuario que creó la solicitud.
 * @property estado Estado actual del proceso de envío.
 * @property fechaRecoleccion Fecha programada para la recolección.
 * @property franjaHoraria Franja horaria programada.
 * @property direccionCompleta La dirección de recolección/entrega aplanada (String).
 * @property guia La información esencial de la guía (GuiaResponse).
 */
data class SolicitudResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("clientId")
    val clientId: Long,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fechaRecoleccion")
    val fechaRecoleccion: String,

    @SerializedName("franjaHoraria")
    val franjaHoraria: String,

    @SerializedName("direccionCompleta")
    val direccionCompleta: String,

    @SerializedName("guia")
    // Usamos el DTO de respuesta simplificado de la guía que ya definimos.
    val guia: GuiaResponse
) : Serializable