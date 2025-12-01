package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.time.Instant // Si tu proyecto Android soporta java.time (generalmente con librerías como ThreeTenABP)

/**
 * Representa un cliente, utilizada para mapear los datos recibidos desde
 * el API REST del backend.
 */
data class Cliente(

    @SerializedName("id")
    val id: Long?,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("tipoId")
    val tipoId: String? = null,

    @SerializedName("numeroId")
    val numeroId: String,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("codigoPais")
    val codigoPais: String? = null,

    @SerializedName("tipoCliente")
    val tipoCliente: String? = null,

    @SerializedName("fechaCreacion")
    // El backend envía Instant, que se serializa como String (ISO 8601)
    val fechaCreacion: String? = null
    // Opcional: Si necesitas trabajar con objetos Instant en Android:
    // val fechaCreacion: Instant = Instant.now()
)

// IMPORTANTE: NO incluyas las propiedades de las listas de Solicitud:
// solicitudesComoRemitente: List<Solicitud>
// solicitudesComoReceptor: List<Solicitud>
// Esas propiedades no se envían por REST y causarían errores de parsing.