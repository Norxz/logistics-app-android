// co.edu.unipiloto.myapplication.dto.SolicitudRequest.kt
package co.edu.unipiloto.myapplication.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📨 DTO principal utilizado por el frontend para iniciar una nueva Solicitud de Envío.
 * Anida la información de clientes, dirección, paquete, y programación.
 *
 * @property clientId ID del usuario logueado que crea la solicitud.
 * @property remitente Datos del cliente que envía (usando ClienteRequest).
 * @property receptor Datos del cliente que recibe (usando ClienteRequest).
 * @property direccion Datos de la dirección de entrega (usando DireccionRequest).
 * @property paquete Especificaciones del paquete (usando PaqueteRequest).
 * @property fechaRecoleccion Fecha programada de recolección (String).
 * @property franjaHoraria Franja horaria programada (String).
 * @property sucursalId ID de la sucursal asignada o seleccionada (requerido por el backend).
 */
data class SolicitudRequest(
    @SerializedName("clientId")
    val clientId: Long,

    @SerializedName("remitente")
    val remitente: ClienteRequest,

    @SerializedName("receptor")
    val receptor: ClienteRequest,

    @SerializedName("direccionRecoleccion")
    val direccionRecoleccion: DireccionRequest?,

    @SerializedName("direccionEntrega")
    val direccionEntrega: DireccionRequest,

    @SerializedName("paquete")
    val paquete: PaqueteRequest,

    @SerializedName("fechaRecoleccion")
    val fechaRecoleccion: String,

    @SerializedName("franjaHoraria")
    val franjaHoraria: String,

    @SerializedName("sucursalId")
    // CAMPO AÑADIDO: Requerido por el SolicitudService del backend
    val sucursalId: Long
) : Serializable