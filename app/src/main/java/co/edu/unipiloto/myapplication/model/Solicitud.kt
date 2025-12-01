// co.edu.unipiloto.myapplication.model.Solicitud.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Entidad central que representa la Solicitud de Envío con todas sus relaciones.
 */
data class Solicitud(

    @SerializedName("id")
    val id: Long?,

    // ----------------------------
    // Relaciones Obligatorias
    // ----------------------------

    @SerializedName("client")
    val client: User, // Asumiendo que tienes el modelo User definido

    @SerializedName("remitente")
    val remitente: Cliente, // Ya definimos este modelo

    @SerializedName("receptor")
    val receptor: Cliente, // Ya definimos este modelo

    @SerializedName("sucursal")
    val sucursal: Sucursal, // Asumiendo que tienes el modelo Sucursal definido

    @SerializedName("direccionRecoleccion")
    val direccionRecoleccion: Direccion? = null,

    @SerializedName("direccionEntrega")
    val direccionEntrega: Direccion,

    @SerializedName("paquete")
    val paquete: Paquete, // Asumiendo que tienes el modelo Paquete definido

    @SerializedName("guia")
    val guia: Guia, // Ya definimos este modelo

    // ----------------------------
    // Asignaciones de personal (Opcionales/Mutables)
    // ----------------------------

    @SerializedName("conductor")
    val conductor: User? = null,

    @SerializedName("gestor")
    val gestor: User? = null,

    @SerializedName("funcionario")
    val funcionario: User? = null,

    // ----------------------------
    // Tiempos y Programación
    // ----------------------------

    @SerializedName("fechaAsignacionConductor")
    val fechaAsignacionConductor: String? = null, // Instant serializado a String

    @SerializedName("fechaRecoleccionReal")
    val fechaRecoleccionReal: String? = null,

    @SerializedName("fechaEntregaReal")
    val fechaEntregaReal: String? = null,

    @SerializedName("fechaRecoleccion")
    val fechaRecoleccion: String, // Fecha programada

    @SerializedName("franjaHoraria")
    val franjaHoraria: String, // Franja horaria programada

    // ----------------------------
    // Control
    // ----------------------------

    @SerializedName("estado")
    val estado: String, // Mapeado del Enum a String

    @SerializedName("createdAt")
    val createdAt: String, // Instant serializado a String

    @SerializedName("motivoCancelacion")
    val motivoCancelacion: String? = null

) : Serializable