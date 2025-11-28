// co.edu.unipiloto.myapplication.api.SolicitudApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.SolicitudRequest
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.model.Solicitud
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 📦 Interfaz de Retrofit para el dominio de Solicitudes de envío.
 * Prefijo Base Asumido: /api/v1/solicitudes
 */
interface SolicitudApi {

    // --- CRUD BÁSICO ---

    /**
     * Mapea a: POST /api/v1/solicitudes
     * Crea una nueva solicitud. El backend retorna el DTO de respuesta simplificado.
     */
    @POST("solicitudes")
    suspend fun crearSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>

    /**
     * Mapea a: GET /api/v1/solicitudes/client/{clientId}
     * Obtiene todas las solicitudes de un cliente. El backend retorna la lista de DTOs simplificados.
     */
    @GET("solicitudes/client/{clientId}")
    suspend fun getSolicitudesByClient(@Path("clientId") clientId: Long): Response<List<SolicitudResponse>>

    /**
     * Mapea a: PUT /api/v1/solicitudes/{solicitudId}/estado
     * Actualiza el estado de una solicitud (e.g., CANCELADA).
     */
    @PUT("solicitudes/{solicitudId}/estado")
    suspend fun updateEstado(
        @Path("solicitudId") solicitudId: Long,
        // Backend espera: {"estado": "NUEVO_ESTADO"}
        @Body estadoUpdate: Map<String, String>
    ): Response<Void> // El backend retorna 204 No Content

    // --- UTILIDAD Y DOCUMENTACIÓN ---

    /**
     * Mapea a: GET /api/v1/solicitudes/{id}/pdf
     * Genera y retorna la guía de la solicitud como un flujo de bytes PDF.
     */
    @GET("solicitudes/{id}/pdf")
    suspend fun generarPdf(@Path("id") id: Long): Response<ResponseBody>

    // --- ASIGNACIONES (Requiere Roles Operativos) ---

    /**
     * Mapea a: POST /api/v1/solicitudes/{solicitudId}/asignar-gestor/{gestorId}
     * Asigna un gestor a una solicitud.
     */
    @POST("solicitudes/{solicitudId}/asignar-gestor/{gestorId}")
    suspend fun asignarGestor(
        @Path("solicitudId") solicitudId: Long,
        @Path("gestorId") gestorId: Long
    ): Response<SolicitudResponse> // Retorna el DTO de respuesta actualizado

    /**
     * Mapea a: POST /api/v1/solicitudes/{solicitudId}/asignar-conductor?gestorId={gestorId}&conductorId={conductorId}
     * Asigna un conductor a una solicitud.
     */
    @POST("solicitudes/{solicitudId}/asignar-conductor")
    suspend fun asignarConductor(
        @Path("solicitudId") solicitudId: Long,
        // Se usan @Query para mapear los @RequestParam del backend
        @Query("gestorId") gestorId: Long,
        @Query("conductorId") conductorId: Long
    ): Response<SolicitudResponse> // Retorna el DTO de respuesta actualizado
}