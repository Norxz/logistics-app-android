// co.edu.unipiloto.myapplication.api.GuideApi.kt
package co.edu.unipiloto.myapplication.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * 📄 Interfaz de Retrofit para gestionar la descarga de documentos (Guías PDF).
 * Prefijo Base Asumido: /api/v1/guia
 */
interface GuideApi {

    /**
     * Endpoint para descargar la guía de una solicitud como archivo PDF.
     * Mapea a: GET /api/v1/guia/download/{id}
     *
     * @param id ID de la solicitud (que se usa para generar la guía).
     * @return ResponseBody que contiene el flujo de bytes del archivo PDF.
     */
    @GET("guia/download/{id}")
    @Streaming // ¡IMPORTANTE! Maneja el flujo de datos para archivos grandes.
    suspend fun downloadGuidePdf(@Path("id") id: Long): Response<ResponseBody>
}