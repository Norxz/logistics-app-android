// Archivo: co.edu.unipiloto.myapplication.dto/SucursalResponse.kt

package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.model.Direccion
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 📦 DTO que mapea la respuesta JSON de una Sucursal.
 * Incluye la información de [Direccion] necesaria para la UI, especialmente el selector de sucursales.
 *
 * @property id Identificador único de la sucursal.
 * @property nombre Nombre comercial de la sucursal.
 * @property direccion Objeto anidado [Direccion] con los detalles de la ubicación.
 */
data class SucursalResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("nombre")
    val nombre: String,

    // 🎯 Incluimos el campo 'direccion' que ahora existe en el DTO del backend
    @SerializedName("direccion")
    val direccion: Direccion // Debe ser no nula si el backend garantiza que siempre existe
) : Serializable