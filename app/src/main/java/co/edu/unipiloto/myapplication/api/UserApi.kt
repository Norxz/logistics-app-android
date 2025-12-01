// co.edu.unipiloto.myapplication.api.UserApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.UserResponse
import co.edu.unipiloto.myapplication.model.Sucursal
import co.edu.unipiloto.myapplication.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

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

    @GET("users/logistic/{id}")
    fun getLogisticUserById(@Path("id") id: Long): Call<User>

    @GET("sucursales")
    fun getAllSucursales(): Call<List<Sucursal>>

    @PUT("users/logistic/{id}")
    fun updateLogisticUser(
        @Path("id") id: Long,
        @Body user: User
    ): Call<User>

    @GET("users/logistic")
    fun getAllLogisticUsers(): Call<List<User>>

    @DELETE("users/logistic/{id}")
    fun deleteLogisticUser(@Path("id") id: Long): Call<Void>

    @GET("drivers/available")
    fun getAvailableDriverBySucursal(@Query("sucursalId") sucursalId: Long): Call<User>

    /**
     * Obtiene una lista de usuarios con el rol de conductor/recolector disponibles para asignación.
     */
    @GET("users/drivers/available") // 🚨 Verifica este endpoint con tu backend
    fun getDriversForAssignment(): Call<List<User>>

    @GET("api/v1/users/conductores") // ⬅️ Usa el endpoint REST real para todos los conductores
    suspend fun getAllConductores(): Response<List<UserResponse>>
}