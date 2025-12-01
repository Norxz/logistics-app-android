package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.SucursalApi
import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import java.lang.Exception
import retrofit2.Response

/**
 * 🏢 Repositorio que maneja la lógica de datos y las llamadas al API
 * para el dominio de Sucursales (oficinas de envío).
 *
 * Este repositorio implementa el patrón Repository, separando el ViewModel
 * de la fuente de datos (en este caso, la API de Retrofit).
 *
 * @property sucursalApi Interfaz de Retrofit para realizar las llamadas de red.
 */
class SucursalRepository(private val sucursalApi: SucursalApi) {

    // --- 1. CONSULTA DE LISTADO (GET /sucursales) ---

    /**
     * Obtiene la lista de todas las sucursales disponibles.
     *
     * @return [Result] con una lista de [SucursalResponse] en caso de éxito, o un [Exception] en caso de error.
     */
    suspend fun listarSucursales(): Result<List<SucursalResponse>> = handleApiCall {
        sucursalApi.listarSucursales()
    }

    // --- 2. CREACIÓN (POST /sucursales) ---

    /**
     * Crea una nueva sucursal enviando el DTO de solicitud.
     *
     * @param request DTO que contiene el nombre y la dirección de la nueva sucursal.
     * @return [Result] con la [SucursalResponse] creada en caso de éxito.
     */
    suspend fun crearSucursal(request: SucursalRequest): Result<SucursalResponse> = handleApiCall {
        sucursalApi.crearSucursal(request)
    }

    // --- 3. OBTENER POR ID (GET /sucursales/{id}) ---

    /**
     * Obtiene una sucursal específica por su identificador único.
     *
     * @param id ID de la sucursal a obtener.
     * @return [Result] con la [SucursalResponse] encontrada.
     */
    suspend fun obtenerSucursal(id: Long): Result<SucursalResponse> = handleApiCall {
        sucursalApi.obtenerSucursal(id)
    }

    // --- 4. ACTUALIZACIÓN (PUT /sucursales/{id}) ---

    /**
     * Actualiza la información de una sucursal existente.
     *
     * @param id ID de la sucursal a actualizar.
     * @param request DTO con los datos actualizados.
     * @return [Result] con la [SucursalResponse] actualizada.
     */
    suspend fun actualizarSucursal(id: Long, request: SucursalRequest): Result<SucursalResponse> = handleApiCall {
        sucursalApi.actualizarSucursal(id, request)
    }

    // --- 5. ELIMINACIÓN (DELETE /sucursales/{id}) ---

    /**
     * Elimina una sucursal por su ID.
     *
     * @param id ID de la sucursal a eliminar.
     * @return [Result] con [Unit] si la eliminación fue exitosa (código 204 No Content).
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

    // --- 6. BÚSQUEDA DE CERCANA (FUNCIÓN CLAVE PARA NewRequestCreationViewModel) ---

    /**
     * 📍 Busca la sucursal geográficamente más cercana a las coordenadas de recolección dadas.
     *
     * Esta función es esencial para asignar automáticamente las solicitudes de los clientes.
     *
     * @param lat Latitud de la ubicación de recolección.
     * @param lon Longitud de la ubicación de recolección.
     * @return [Result] con el DTO completo de la [SucursalResponse] más cercana.
     */
    suspend fun findNearestSucursal(lat: Double, lon: Double): Result<SucursalResponse> = handleApiCall {
        sucursalApi.getNearestSucursal(lat, lon)
    }

    // --- ALIAS Y UTILITARIOS ---

    /**
     * Alias para [crearSucursal], utilizado por ViewModels para la operación de guardado.
     *
     * @param request DTO con la información de la nueva sucursal.
     * @return [Result] con la [SucursalResponse] creada.
     */
    suspend fun saveBranch(request: SucursalRequest): Result<SucursalResponse> {
        return crearSucursal(request)
    }

    /**
     * Alias para [actualizarSucursal], utilizado por ViewModels para la operación de edición.
     *
     * @param id ID de la sucursal a actualizar (recibido como Int desde la UI).
     * @param request DTO con los datos actualizados.
     * @return [Result] con la [SucursalResponse] actualizada.
     */
    suspend fun updateBranch(id: Int, request: SucursalRequest): Result<SucursalResponse> {
        return actualizarSucursal(id.toLong(), request) // Convierte el Int a Long para la API
    }

    // --- FUNCIÓN UTILITARIA PARA MANEJO DE LLAMADAS ---

    /**
     * Función genérica suspendida para manejar la lógica repetitiva de las respuestas de Retrofit,
     * envolviendo el resultado de la llamada de red en un objeto [Result] de Kotlin.
     *
     * @param call Bloque de código suspendido que contiene la llamada a la API de Retrofit.
     * @return [Result] que contiene el cuerpo de la respuesta en caso de éxito, o una [Exception] en caso de fallo (HTTP o red).
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