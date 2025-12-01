// Archivo: co.edu.unipiloto.myapplication.rest.RetrofitClient.kt
package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.api.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.jvm.java

/**
 * 🔌 Objeto Singleton de Retrofit.
 * * Este objeto centraliza la gestión de la configuración de red y proporciona
 * acceso a las interfaces modulares de la API de la aplicación. Se inicializa
 * de forma perezosa (lazy) la instancia de [Retrofit] y [OkHttpClient].
 */
object RetrofitClient {

    /** * 🚨 Dirección base del servidor Spring Boot.
     * 10.0.2.2 es el alias estándar del host local (localhost) dentro del emulador Android.
     */
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    // --- 1. Cliente HTTP (OkHttpClient) con Interceptor de Logs ---

    /**
     * Interceptor para registrar las peticiones y respuestas HTTP completas en el Logcat.
     * Es vital para la depuración en desarrollo.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Establece el nivel de detalle (BODY muestra los headers y el cuerpo JSON)
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // [TODO]: Añadir aquí el interceptor para el Token de Autorización (e.g., Bearer token)
        .build()

    // --- 2. Instancia Base de Retrofit ---

    /**
     * Instancia de Retrofit inicializada de forma perezosa (lazy).
     * Configurada con la URL base, el conversor GSON y el cliente HTTP personalizado.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // Asigna el cliente con el interceptor
            .client(client)
            .build()
    }

    // --- 3. Propiedades y Métodos de Acceso a las Interfaces Modulares ---

    /** * Obtiene la interfaz para el dominio de Autenticación.
     * Uso: `RetrofitClient.getAuthApi().login(...)`
     */
    fun getAuthApi(): AuthApi = retrofit.create(AuthApi::class.java)

    /** * Obtiene la interfaz para el dominio de Solicitudes (Creación, Listado, etc.).
     * Uso: `RetrofitClient.getSolicitudApi().crearSolicitud(...)`
     */
    fun getSolicitudApi(): SolicitudApi = retrofit.create(SolicitudApi::class.java)

    /** * Obtiene la interfaz para el dominio de Sucursales.
     * Uso: `RetrofitClient.getSucursalApi().getSucursales(...)`
     */
    fun getSucursalApi(): SucursalApi = retrofit.create(SucursalApi::class.java)

    /** * Obtiene la interfaz para la descarga de Guías PDF.
     * Uso: `RetrofitClient.getGuideApi().generarPdf(...)`
     */
    fun getGuideApi(): GuideApi = retrofit.create(GuideApi::class.java)

    /** * Obtiene la interfaz para la gestión de Usuarios Logísticos.
     * Uso: `RetrofitClient.getUserApi().getUserDetails(...)`
     */
    fun getUserApi(): UserApi = retrofit.create(UserApi::class.java)

    /**
     * 💡 Propiedad de acceso alternativa para el dominio de Solicitudes.
     * Permite un acceso más directo y idiomático en Kotlin.
     * Uso: `RetrofitClient.solicitudService.crearSolicitud(...)`
     */
    val solicitudService: SolicitudApi = retrofit.create(SolicitudApi::class.java)

    /**
     * Propiedad de acceso alternativa para el dominio de Sucursales.
     * Uso: `RetrofitClient.sucursalService.listarSucursales(...)`
     */
    val sucursalService: SucursalApi = retrofit.create(SucursalApi::class.java)
}