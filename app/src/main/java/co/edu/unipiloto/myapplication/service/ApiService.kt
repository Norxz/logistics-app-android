package co.edu.unipiloto.myapplication.service


import co.edu.unipiloto.myapplication.model.*
import co.edu.unipiloto.myapplication.rest.* // Importa tus DTOs
import retrofit2.Call
import retrofit2.http.*
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {

    // --- AUTENTICACIÓN Y REGISTRO ---

    @POST("api/v1/auth/login")
    fun login(@Body request: LoginRequest): Call<User>

    @POST("api/v1/auth/register")
    fun register(@Body request: RegisterRequest): Call<User>

    // --- SOLICITUDES (CRUD) ---

    // Crear Solicitud: POST /api/v1/solicitudes
    @POST("api/v1/solicitudes")
    fun crearSolicitud(@Body request: SolicitudRequest): Call<Solicitud>

    // Obtener Solicitudes por Cliente: GET /api/v1/solicitudes/client/{clientId}
    @GET("api/v1/solicitudes/client/{clientId}")
    fun getSolicitudesByClient(@Path("clientId") clientId: Long): Call<List<Solicitud>>

    // Cancelar Solicitud (Actualizar Estado): PUT /api/v1/solicitudes/{solicitudId}/estado
    // Nota: El backend debe estar preparado para recibir un cuerpo {"estado": "CANCELADA"}
    @PUT("api/v1/solicitudes/{solicitudId}/estado")
    fun actualizarEstado(
        @Path("solicitudId") id: Long,
        @Body estado: Map<String, String>
    ): Call<Void>

    @GET("api/v1/tracking/{guideId}")
    fun getShippingStatus(@Path("guideId") guideCode: String): Call<ShippingStatus>

    @GET("api/v1/solicitudes/zone/{zona}/assigned")
    fun getAssignedSolicitudesByZone(@Path("zona") zona: String): Call<List<Solicitud>>


    @GET("api/v1/solicitudes/zone/{zona}/completed")
    fun getCompletedSolicitudesByZone(@Path("zona") zona: String): Call<List<Solicitud>>

    // En ApiService.kt
    @GET("api/v1/solicitudes/driver/{driverId}")
    fun getDriverRoutes(@Path("driverId") driverId: Long): Call<List<Solicitud>>

    @GET("api/v1/logistic-users/{recolectorId}")
    fun getLogisticUserById(@Path("recolectorId") recolectorId: Long): Call<LogisticUser>

    @PUT("api/v1/logistic-users/{recolectorId}")
    fun updateLogisticUser(
        @Path("recolectorId") recolectorId: Long,
        @Body user: LogisticUser
    ): Call<LogisticUser>

    @GET("api/v1/logistic-users")
    fun getAllLogisticUsers(): Call<List<LogisticUser>>

    @DELETE("api/v1/logistic-users/{userId}")
    fun deleteLogisticUser(@Path("userId") userId: Long): Call<Void>

    @GET("api/v1/solicitudes/zone/{zona}/pending")
    fun getPendingSolicitudesByZone(@Path("zona") zona: String): Call<List<Solicitud>>

    @GET("api/v1/logistic-users/available/{zona}")
    fun getAvailableDriverByZone(@Path("zona") zona: String): Call<LogisticUser>

    @PUT("api/v1/solicitudes/{solicitudId}/assign")
    fun assignRequest(
        @Path("solicitudId") solicitudId: Long,
        @Body recolectorId: Map<String, String>
    ): Call<Solicitud>

    @GET("api/v1/logistic-users/drivers") // Endpoint para obtener lista de conductores activos
    fun getDriversForAssignment(): Call<List<LogisticUser>>

    // En ApiService.kt
    @GET("api/v1/solicitudes/all")
    fun getAllRequests(): Call<List<Request>>

    @GET("api/v1/guia/download/{id}")
    @Streaming
    suspend fun downloadGuidePdf(@Path("id") solicitudId: Long): Response<ResponseBody>

    @GET("api/v1/guia/{id}")
    suspend fun getGuiaInfo(@Path("id") solicitudId: Long): Response<GuiaResponse>

    @GET("api/v1/sucursales")
    fun getSucursales(): Call<List<Sucursal>>

    @GET("solicitudes/sucursal/{id}")
    fun getSolicitudesBySucursal(
        @Path("id") sucursalId: Long
    ): Call<List<Solicitud>>

    @GET("users/recolectores/disponible/{sucursalId}")
    fun getAvailableDriverBySucursal(
        @Path("sucursalId") sucursalId: Long
    ): Call<LogisticUser>
}