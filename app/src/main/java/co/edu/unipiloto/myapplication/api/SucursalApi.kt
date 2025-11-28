// co.edu.unipiloto.myapplication.api.SucursalApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 🏢 Interfaz de Retrofit para gestionar la información de las sucursales (CRUD).
 * Prefijo Base Asumido: /api/v1/sucursales
 */
interface SucursalApi {

    /**
     * Mapea a: GET /api/v1/sucursales
     * Lista todas las sucursales.
     * @return Lista de DTOs simplificados (SucursalResponse).
     */
    @GET("sucursales")
    suspend fun listarSucursales(): Response<List<SucursalResponse>>

    /**
     * Mapea a: POST /api/v1/sucursales
     * Crea una nueva sucursal.
     * @param request DTO con el nombre y dirección.
     * @return DTO de la sucursal creada.
     */
    @POST("sucursales")
    suspend fun crearSucursal(@Body request: SucursalRequest): Response<SucursalResponse>

    /**
     * Mapea a: GET /api/v1/sucursales/{id}
     * Obtiene una sucursal específica.
     * @param id ID de la sucursal.
     * @return DTO de la sucursal.
     */
    @GET("sucursales/{id}")
    suspend fun obtenerSucursal(@Path("id") id: Long): Response<SucursalResponse>

    /**
     * Mapea a: PUT /api/v1/sucursales/{id}
     * Actualiza una sucursal existente.
     * @param id ID de la sucursal a actualizar.
     * @param request DTO con los datos actualizados.
     * @return DTO de la sucursal actualizada.
     */
    @PUT("sucursales/{id}")
    suspend fun actualizarSucursal(
        @Path("id") id: Long,
        @Body request: SucursalRequest
    ): Response<SucursalResponse>

    /**
     * Mapea a: DELETE /api/v1/sucursales/{id}
     * Elimina una sucursal.
     */
    @DELETE("sucursales/{id}")
    suspend fun eliminarSucursal(@Path("id") id: Long): Response<Void> // Retorna 204 No Content
}