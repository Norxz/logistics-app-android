// co.edu.unipiloto.myapplication.repository.AuthRepository.kt
package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.AuthApi
import co.edu.unipiloto.myapplication.dto.LoginRequest
import co.edu.unipiloto.myapplication.dto.RegisterRequest
import co.edu.unipiloto.myapplication.dto.UserResponse
import java.lang.Exception
import retrofit2.Response

/**
 * 🔑 Repositorio que maneja la lógica de Autenticación (Login y Registro).
 * Es responsable de llamar a la AuthApi, manejar los errores HTTP y gestionar la sesión local.
 */
class AuthRepository(private val authApi: AuthApi) {

    /**
     * Intenta iniciar sesión.
     * Mapea a: POST /api/v1/auth/login
     *
     * @param request DTO con el email y la contraseña.
     * @return Result.Success con el DTO del usuario autenticado, o Result.Failure.
     */
    suspend fun login(request: LoginRequest): Result<UserResponse> {
        return try {
            val response: Response<UserResponse> = authApi.login(request)

            if (response.isSuccessful && response.body() != null) {
                // Éxito: (200 OK)
                val userResponse = response.body()!!
                // NOTA: Aquí deberías guardar el token JWT si lo usaras, o al menos el UserResponse
                Result.success(userResponse)
            } else {
                // Fallo de API: (ej. 401 UNAUTHORIZED)
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales inválidas. Verifica tu email y contraseña."
                    else -> response.errorBody()?.string() ?: "Error de servidor al iniciar sesión."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Fallo de red (sin conexión, timeout)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    /**
     * Intenta registrar un nuevo usuario.
     * Mapea a: POST /api/v1/auth/register
     *
     * @param request DTO con los datos de registro.
     * @return Result.Success con el DTO del usuario creado, o Result.Failure.
     */
    suspend fun register(request: RegisterRequest): Result<UserResponse> {
        return try {
            val response: Response<UserResponse> = authApi.register(request)

            if (response.isSuccessful && response.body() != null) {
                // Éxito: (201 CREATED)
                Result.success(response.body()!!)
            } else {
                // Fallo de API: (ej. 409 CONFLICT)
                val errorMessage = when (response.code()) {
                    409 -> "El email o identificación ya está registrado."
                    else -> response.errorBody()?.string() ?: "Error de servidor al registrar."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Fallo de red
            Result.failure(Exception("Error de conexión. Intenta nuevamente."))
        }
    }
}