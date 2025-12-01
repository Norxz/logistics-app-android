package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.UserApi
import co.edu.unipiloto.myapplication.dto.UserResponse
import java.lang.Exception
import retrofit2.Response

/**
 * 👤 Repositorio que gestiona la lógica de datos y las llamadas al API
 * para el dominio de Usuarios (especialmente logísticos: Gestores y Conductores).
 * Implementa consultas por rol/sucursal y gestión (asignación, desactivación, eliminación).
 */
class UserRepository(private val userApi: UserApi) {

    // --- 1. ASIGNACIÓN Y ACTUALIZACIÓN ---

    /**
     * Asigna una sucursal a un usuario. Mapea a: PUT /api/v1/users/{userId}/sucursal/{sucursalId}
     */
    suspend fun asignarSucursal(userId: Long, sucursalId: Long): Result<UserResponse> = handleApiCall {
        userApi.asignarSucursal(userId, sucursalId)
    }

    /**
     * Desactiva un usuario. Mapea a: PUT /api/v1/users/{userId}/desactivar
     */
    suspend fun desactivarUsuario(userId: Long): Result<UserResponse> = handleApiCall {
        userApi.desactivarUsuario(userId)
    }

    // --- 2. CONSULTAS POR ROL Y SUCURSAL ---

    /**
     * Obtiene la lista de todos los usuarios logísticos activos (Gestores y Conductores).
     * Mapea a: GET /api/v1/users/logistic
     */
    suspend fun getLogisticUsers(): Result<List<UserResponse>> = handleApiCall {
        userApi.getLogisticUsers()
    }

    /**
     * Obtiene los gestores activos de una sucursal específica.
     * Mapea a: GET /api/v1/users/gestores/sucursal/{sucursalId}
     */
    suspend fun getGestoresBySucursal(sucursalId: Long): Result<List<UserResponse>> = handleApiCall {
        userApi.getGestoresBySucursal(sucursalId)
    }

    /**
     * Obtiene los conductores activos de una sucursal específica.
     * Mapea a: GET /api/v1/users/conductores/sucursal/{sucursalId}
     */
    suspend fun getConductoresBySucursal(sucursalId: Long): Result<List<UserResponse>> = handleApiCall {
        userApi.getConductoresBySucursal(sucursalId)
    }

    // --- 3. ELIMINACIÓN ---

    /**
     * Elimina permanentemente un usuario. Mapea a: DELETE /api/v1/users/{userId}
     * Retorna Unit si la eliminación fue exitosa (204 No Content).
     */
    suspend fun eliminarUsuario(userId: Long): Result<Unit> {
        return try {
            val response: Response<Void> = userApi.eliminarUsuario(userId)

            if (response.isSuccessful) {
                // 204 No Content (éxito sin cuerpo)
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error al eliminar el usuario."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // --- 4. NUEVAS FUNCIONES PARA VIEWMODEL ---

    /**
     * 🌟 NUEVA FUNCIÓN REQUERIDA POR EL VIEWMODEL
     * Obtiene los conductores disponibles (Gestores/Recolectores) para asignación.
     */
    suspend fun getDrivers(): Result<List<UserResponse>> = handleApiCall {
        // Asumiendo que esta función existe en UserApi y retorna todos los conductores.
        userApi.getAllConductores()
    }

    /**
     * 🏆 FUNCIÓN CORREGIDA: Obtiene la lista de Gestores disponibles.
     *
     * Ahora llama a `getGestoresBySucursal(sucursalId)` para garantizar que el
     * `ManagerDashboardViewModel` reciba solo Gestores y no Conductores.
     */
    suspend fun getAvailableManagers(sucursalId: Long): Result<List<UserResponse>> {
        // 🚀 CORRECCIÓN CLAVE: Usar la función que trae a los GESTORES de la sucursal.
        return getGestoresBySucursal(sucursalId)
    }

    // --- FUNCIÓN UTILITARIA PARA MANEJO DE LLAMADAS GENÉRICAS ---

    /**
     * Función genérica para manejar la lógica repetitiva de Retrofit Response,
     * envolviendo el resultado en Result.
     */
    private suspend fun <T : Any> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error de servidor (${response.code()})."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}