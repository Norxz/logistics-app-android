// co.edu.unipiloto.myapplication.api.UserApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.UserResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT

/**
 * 👤 Interfaz de Retrofit para la gestión y consulta de usuarios logísticos.
 * Prefijo Base Asumido: /api/v1/users
 */
interface UserApi {

    // --- GESTIÓN Y ACTUALIZACIÓN ---

    /**
     * Mapea a: PUT /api/v1/users/{userId}/sucursal/{sucursalId}
     * Asigna una sucursal a un usuario.
     */
    @PUT("users/{userId}/sucursal/{sucursalId}")
    suspend fun asignarSucursal(
        @Path("userId") userId: Long,
        @Path("sucursalId") sucursalId: Long
    ): Response<UserResponse>

    /**
     * Mapea a: PUT /api/v1/users/{userId}/desactivar
     * Desactiva la cuenta de un usuario.
     */
    @PUT("users/{userId}/desactivar")
    suspend fun desactivarUsuario(@Path("userId") userId: Long): Response<UserResponse>

    /**
     * Mapea a: DELETE /api/v1/users/{userId}
     * Elimina permanentemente un usuario.
     */
    @DELETE("users/{userId}")
    suspend fun eliminarUsuario(@Path("userId") userId: Long): Response<Void> // Retorna 204 No Content

    // --- CONSULTAS ---

    /**
     * Mapea a: GET /api/v1/users/logistic
     * Obtiene todos los usuarios logísticos activos (Gestores y Conductores).
     */
    @GET("users/logistic")
    suspend fun getLogisticUsers(): Response<List<UserResponse>>

    /**
     * Mapea a: GET /api/v1/users/gestores/sucursal/{sucursalId}
     * Obtiene los gestores activos de una sucursal.
     */
    @GET("users/gestores/sucursal/{sucursalId}")
    suspend fun getGestoresBySucursal(@Path("sucursalId") sucursalId: Long): Response<List<UserResponse>>

    /**
     * Mapea a: GET /api/v1/users/conductores/sucursal/{sucursalId}
     * Obtiene los conductores activos de una sucursal.
     */
    @GET("users/conductores/sucursal/{sucursalId}")
    suspend fun getConductoresBySucursal(@Path("sucursalId") sucursalId: Long): Response<List<UserResponse>>
}