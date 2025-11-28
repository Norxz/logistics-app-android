// co.edu.unipiloto.myapplication.model.Paquete.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Representa un paquete con sus dimensiones y contenido,
 * mapeando los datos recibidos del API REST.
 */
data class Paquete(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("peso")
    val peso: Double = 0.0,

    @SerializedName("alto")
    val alto: Double? = null,

    @SerializedName("ancho")
    val ancho: Double? = null,

    @SerializedName("largo")
    val largo: Double? = null,

    @SerializedName("contenido")
    val contenido: String? = null,

    @SerializedName("categoria")
    val categoria: String? = null
) : Serializable