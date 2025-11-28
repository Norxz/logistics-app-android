// co.edu.unipiloto.myapplication.rest.RetrofitClient.kt
package co.edu.unipiloto.myapplication.rest

import co.edu.unipiloto.myapplication.api.* // Importa las 5 interfaces: AuthApi, SolicitudApi, etc.
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 🔌 Objeto Singleton de Retrofit.
 * Encargado de inicializar la configuración de red y proporcionar acceso
 * a las interfaces modulares de la API.
 */
object RetrofitClient {

    // 🚨 Dirección base de tu servidor Spring Boot.
    // 10.0.2.2 es el alias del host local (localhost) dentro del emulador Android.
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    // --- 1. Cliente HTTP (OkHttpClient) con Interceptor de Logs ---

    // El interceptor de logging es vital para ver las peticiones/respuestas en el Logcat durante el desarrollo.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Establece el nivel de detalle (BODY muestra el JSON completo)
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Puedes añadir aquí el interceptor para el Token de Autorización si lo usas
        .build()

    // --- 2. Instancia Base de Retrofit ---

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // Asigna el cliente con el interceptor
            .client(client)
            .build()
    }

    // --- 3. Métodos de Acceso a las Interfaces Modulares ---

    /** Obtiene la interfaz para el dominio de Autenticación. */
    fun getAuthApi(): AuthApi = retrofit.create(AuthApi::class.java)

    /** Obtiene la interfaz para el dominio de Solicitudes. */
    fun getSolicitudApi(): SolicitudApi = retrofit.create(SolicitudApi::class.java)

    /** Obtiene la interfaz para el dominio de Sucursales. */
    fun getSucursalApi(): SucursalApi = retrofit.create(SucursalApi::class.java)

    /** Obtiene la interfaz para la descarga de Guías PDF. */
    fun getGuideApi(): GuideApi = retrofit.create(GuideApi::class.java)

    /** Obtiene la interfaz para la gestión de Usuarios Logísticos. */
    fun getUserApi(): UserApi = retrofit.create(UserApi::class.java)
}