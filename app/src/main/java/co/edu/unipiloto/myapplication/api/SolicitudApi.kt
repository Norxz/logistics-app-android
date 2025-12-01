// co.edu.unipiloto.myapplication.api.SolicitudApi.kt
package co.edu.unipiloto.myapplication.api

import co.edu.unipiloto.myapplication.dto.SolicitudRequest
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import okhttp3.ResponseBody
import retrofit2.Call
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
     * Mapea a: GET /api/v1/solicitudes/tracking/{trackingNumber}
     * Obtiene una solicitud de envío utilizando su número de rastreo (trackingNumber) de la guía.
     *
     * @param trackingNumber El número de guía único utilizado para el rastreo.
     * @return El DTO de respuesta simplificado de la solicitud encontrada.
     */
    @GET("solicitudes/tracking/{trackingNumber}")
    suspend fun getSolicitudByTrackingNumber(@Path("trackingNumber") trackingNumber: String): Response<SolicitudResponse>

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
        @Query("gestorId") gestorId: Long,
        @Query("conductorId") conductorId: Long
    ): Response<SolicitudResponse>

    /**
     * Mapea a: GET /api/v1/solicitudes/branch/{sucursalId}
     * Obtiene todas las solicitudes de una sucursal para que el Gestor pueda filtrar/asignar.
     * Retorna la lista de DTOs simplificados.
     */
    @GET("solicitudes/branch/{sucursalId}")
    suspend fun getSolicitudesBySucursal(@Path("sucursalId") sucursalId: Long): Response<List<SolicitudResponse>>

    /**
     * Mapea a: GET /api/v1/solicitudes/branch/{sucursalId}/assigned
     * Obtiene todas las solicitudes de una sucursal que ya están ASIGNADAS a un conductor.
     * Esto soporta la pestaña "Asignadas" del Gestor.
     */
    @GET("solicitudes/branch/{sucursalId}/assigned")
    fun getAssignedSolicitudesBySucursal(@Path("sucursalId") sucursalId: Long): retrofit2.Call<List<SolicitudResponse>>

    /**
     * Mapea a: PUT /api/v1/solicitudes/{solicitudId}/assign-driver
     * Asigna o reasigna un conductor a una solicitud.
     * El cuerpo espera: {"recolectorId": "ID_CONDUCTOR"}
     */
    @PUT("solicitudes/{solicitudId}/assign-driver")
    fun assignRequest(
        @Path("solicitudId") solicitudId: Long,
        // Backend espera: {"recolectorId": "ID_CONDUCTOR"}
        @Body body: Map<String, String>
    ): retrofit2.Call<SolicitudResponse>

    /**
     * Mapea a: GET /api/v1/solicitudes/branch/{sucursalId}/completed
     * Obtiene todas las solicitudes de una sucursal que ya están FINALIZADAS (Entregadas o Canceladas).
     * Esto soporta la pestaña "Completadas" del Gestor/Funcionario.
     *
     * @param sucursalId El ID numérico de la sucursal.
     * @return Una lista de DTOs simplificados.
     */
    @GET("solicitudes/branch/{sucursalId}/completed")
    fun getCompletedSolicitudesBySucursal(@Path("sucursalId") sucursalId: Long): retrofit2.Call<List<SolicitudResponse>>

    /**
     * Mapea a: GET /api/v1/solicitudes/driver/{driverId}/routes
     * Obtiene todas las solicitudes de envío ASIGNADAS a un conductor específico.
     *
     * @param driverId El ID del conductor.
     * @return Una lista de DTOs simplificados (SolicitudResponse).
     */
    @GET("solicitudes/driver/{driverId}/routes")
    fun getRoutesByDriverId(@Path("driverId") driverId: Long): retrofit2.Call<List<SolicitudResponse>>

    /**
     * Actualiza el estado de una solicitud.
     * @param requestId El ID de la solicitud a modificar.
     * @param body Mapa que contiene el nuevo estado (ej: {"estado": "ASIGNADO"}).
     * @return Retorna Call<Void> si la respuesta es 204 No Content.
     */
    @PUT("solicitudes/{id}/status") // 🚨 Verifica este endpoint
    fun actualizarEstado(@Path("id") requestId: Long, @Body body: Map<String, String>): Call<Void>

    /**
     * Obtiene todas las solicitudes del sistema (visible para ADMIN).
     */
    @GET("solicitudes")
    fun getAllSolicitudes(): Call<List<SolicitudResponse>>

    @PUT("api/v1/solicitudes/{id}/assign")
    suspend fun assignDriverEndpoint(@Path("id") solicitudId: Long, @Body body: Map<String, Long>): Response<SolicitudResponse>

    @PUT("api/v1/solicitudes/{id}/assignGestor")
    suspend fun assignGestorEndpoint(
        @Path("id") solicitudId: Long,
        @Body body: Map<String, Long>
    ): Response<SolicitudResponse>
}