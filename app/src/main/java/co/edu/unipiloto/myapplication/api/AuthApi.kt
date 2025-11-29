// co.edu.unipiloto.myapplication.api.AuthApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.LoginRequest
import co.edu.unipiloto.myapplication.dto.RegisterRequest
import co.edu.unipiloto.myapplication.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 🔑 Interfaz de Retrofit para gestionar la autenticación y el registro de usuarios.
 * Mapea directamente a los endpoints del AuthController del backend.
 */
interface AuthApi {

    /**
     * Endpoint para registrar un nuevo usuario.
     * Mapea a: POST /api/v1/auth/register
     *
     * @param request DTO con los datos del nuevo usuario.
     * @return El Response de Retrofit con el DTO del usuario creado.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    /**
     * Endpoint para iniciar sesión de un usuario existente.
     * Mapea a: POST /api/v1/auth/login
     *
     * @param request DTO con email y password.
     * @return El Response de Retrofit con el DTO del usuario autenticado.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>
}