// co.edu.unipiloto.myapplication.repository.SolicitudRepository.kt
package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.dto.SolicitudRequest
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import okhttp3.ResponseBody
import java.lang.Exception
import retrofit2.Response
import java.io.IOException

/**
 * 📦 Repositorio que gestiona la lógica de datos y las llamadas al API
 * para el dominio de Solicitudes (creación, consulta, estado, asignación).
 * Utiliza la SolicitudApi para la comunicación con el backend.
 */
class SolicitudRepository(private val solicitudApi: SolicitudApi) {

    // --- 1. CREACIÓN ---

    /**
     * Crea una nueva solicitud de recolección. Mapea a: POST /api/v1/solicitudes
     */
    suspend fun crearSolicitud(request: SolicitudRequest): Result<SolicitudResponse> = handleApiCall {
        solicitudApi.crearSolicitud(request)
    }

    // --- 2. CONSULTA ---

    /**
     * Obtiene todas las solicitudes de un cliente específico. Mapea a: GET /api/v1/solicitudes/client/{clientId}
     */
    suspend fun getSolicitudesByClient(clientId: Long): Result<List<SolicitudResponse>> = handleApiCall {
        solicitudApi.getSolicitudesByClient(clientId)
    }

    /**
     * Obtiene todas las solicitudes asignadas a una sucursal específica (para el Manager).
     */
    suspend fun getSolicitudesByBranch(branchId: Long): Result<List<SolicitudResponse>> = handleApiCall {
        // 🏆 CORRECCIÓN: Usa 'solicitudApi' y llama a la función suspend de la API
        solicitudApi.getSolicitudesBySucursal(branchId)
    }

    // --- 3. ACTUALIZACIÓN DE ESTADO ---

    /**
     * Actualiza el estado de una solicitud (ej. "CANCELADA"). Mapea a: PUT /api/v1/solicitudes/{solicitudId}/estado
     * El backend espera un Map<String, String> con la clave "estado".
     */
    suspend fun updateEstado(solicitudId: Long, newState: String): Result<Unit> {
        val estadoMap = mapOf("estado" to newState)

        return try {
            // El backend retorna 204 No Content, por eso usamos Response<Void>
            val response: Response<Void> = solicitudApi.updateEstado(solicitudId, estadoMap)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error al actualizar el estado."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // --- 4. ASIGNACIONES ---

    /**
     * Asigna un gestor a una solicitud. Mapea a: POST /api/v1/solicitudes/{solicitudId}/asignar-gestor/{gestorId}
     */
    suspend fun asignarGestor(solicitudId: Long, gestorId: Long): Result<SolicitudResponse> = handleApiCall {
        solicitudApi.asignarGestor(solicitudId, gestorId)
    }

    /**
     * Asigna un conductor a una solicitud. Mapea a: POST /api/v1/solicitudes/{solicitudId}/asignar-conductor
     * Nota: Utiliza parámetros de consulta (@Query) para gestorId y conductorId.
     */
    suspend fun asignarConductor(
        solicitudId: Long,
        gestorId: Long,
        conductorId: Long
    ): Result<SolicitudResponse> = handleApiCall {
        solicitudApi.asignarConductor(solicitudId, gestorId, conductorId)
    }

    // --- 5. OBTENER PDF (DOCUMENTO) ---

    /**
     * Genera y obtiene el PDF de la solicitud. Mapea a: GET /api/v1/solicitudes/{id}/pdf
     * NOTA: La lógica para guardar el archivo está mejor en el GuideRepository,
     * pero aquí llamamos al endpoint si es necesario.
     */
    suspend fun generarPdf(solicitudId: Long): Result<ResponseBody> = handleApiCall {
        solicitudApi.generarPdf(solicitudId)
    }

    // --- FUNCIÓN UTILITARIA PARA MANEJO DE LLAMADAS GENÉRICAS ---

    /**
     * Función genérica para manejar la lógica repetitiva de Result, isSuccessful y errorBody.
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

    /**
     * 🏆 IMPLEMENTACIÓN FALTANTE
     * Asigna un conductor a una solicitud específica, resolviendo el error del ViewModel.
     * Mapea a: PUT /api/v1/solicitudes/{solicitudId}/assign
     */
    suspend fun assignDriver(solicitudId: Long, driverId: Long): Result<SolicitudResponse> {
        // Prepara el cuerpo de la solicitud JSON con el ID del conductor
        val body = mapOf("recolectorId" to driverId)

        // Usa la función handleApiCall (si la tienes definida en el repositorio)
        // o realiza el try-catch manual.
        return handleApiCall {
            // 🚨 DEBES CAMBIAR 'assignDriverEndpoint' por el nombre real del método
            // que definiste en tu interfaz SolicitudApi.
            solicitudApi.assignDriverEndpoint(solicitudId, body)
        }
    }

    /**
     * 🏆 IMPLEMENTACIÓN FALTANTE
     * Asigna un gestor a una solicitud específica, resolviendo el error del ViewModel.
     * Mapea a: PUT /api/v1/solicitudes/{solicitudId}/assignGestor
     */
    suspend fun assignGestor(solicitudId: Long, gestorId: Long): Result<SolicitudResponse> {
        return try {
            // Asegúrate de que esta llamada NO esté usando un ID o campo de Conductor.
            val response = solicitudApi.assignGestor(solicitudId, gestorId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Este mensaje puede ser el que ves. Revisa el cuerpo del error.
                val errorMessage = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Error de conexión o Timeout
            Result.failure(e)
        }
    }
}