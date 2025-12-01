// co.edu.unipiloto.myapplication.dto.SolicitudResponse.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📊 DTO de respuesta simplificado...
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
    val guia: GuiaResponse,

    @SerializedName("recolectorId")
    val recolectorId: Long? = null,

    @SerializedName("recolectorName")
    val recolectorName: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null

    // --------------------------------------------------------

) : Serializable