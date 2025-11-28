// co.edu.unipiloto.myapplication.repository.SucursalRepository.kt
package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.SucursalApi
import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import java.lang.Exception
import retrofit2.Response

/**
 * 🏢 Repositorio que maneja la lógica de datos y las llamadas al API
 * para el dominio de Sucursales (oficinas de envío).
 * * Implementa el CRUD completo: listar, crear, obtener, actualizar y eliminar.
 */
class SucursalRepository(private val sucursalApi: SucursalApi) {

    // --- 1. CONSULTA DE LISTADO (GET /sucursales) ---

    /**
     * Obtiene la lista de todas las sucursales disponibles.
     * Ideal para llenar Spinners o listados.
     */
    suspend fun listarSucursales(): Result<List<SucursalResponse>> = handleApiCall {
        sucursalApi.listarSucursales()
    }

    // --- 2. CREACIÓN (POST /sucursales) ---

    /**
     * Crea una nueva sucursal.
     */
    suspend fun crearSucursal(request: SucursalRequest): Result<SucursalResponse> = handleApiCall {
        sucursalApi.crearSucursal(request)
    }

    // --- 3. OBTENER POR ID (GET /sucursales/{id}) ---

    /**
     * Obtiene una sucursal específica por su identificador.
     */
    suspend fun obtenerSucursal(id: Long): Result<SucursalResponse> = handleApiCall {
        sucursalApi.obtenerSucursal(id)
    }

    // --- 4. ACTUALIZACIÓN (PUT /sucursales/{id}) ---

    /**
     * Actualiza la información de una sucursal existente.
     */
    suspend fun actualizarSucursal(id: Long, request: SucursalRequest): Result<SucursalResponse> = handleApiCall {
        sucursalApi.actualizarSucursal(id, request)
    }

    // --- 5. ELIMINACIÓN (DELETE /sucursales/{id}) ---

    /**
     * Elimina una sucursal por su ID. Retorna Unit si fue 204 No Content.
     */
    suspend fun eliminarSucursal(id: Long): Result<Unit> {
        return try {
            val response: Response<Void> = sucursalApi.eliminarSucursal(id)

            if (response.isSuccessful) {
                // 204 No Content (éxito sin cuerpo)
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error al eliminar la sucursal."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // --- FUNCIÓN UTILITARIA PARA MANEJO DE LLAMADAS (Mismo código que en SolicitudRepository) ---

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