// co.edu.unipiloto.myapplication.model.Sucursal.kt
package co.edu.unipiloto.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Representa una Sucursal de la empresa, mapeando los datos recibidos del API REST.
 * La dirección es un campo obligatorio en el backend.
 */
data class Sucursal(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("direccion")
    // Se elimina la nulabilidad (?) para reflejar la restricción del backend (nullable = false)
    val direccion: Direccion,
) : Serializable