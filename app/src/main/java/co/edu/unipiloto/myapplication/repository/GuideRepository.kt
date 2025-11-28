// co.edu.unipiloto.myapplication.repository.GuideRepository.kt
package co.edu.unipiloto.myapplication.repository

import co.edu.unipiloto.myapplication.api.GuideApi
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import retrofit2.Response

/**
 * 📄 Repositorio que gestiona la descarga y el manejo de documentos (Guías PDF).
 * Implementa la lógica para consumir el endpoint de descarga binaria y guardar el archivo localmente.
 */
class GuideRepository(private val guideApi: GuideApi) {

    // --- DESCARGA DE PDF ---

    /**
     * Descarga el archivo PDF de una guía de solicitud y lo guarda en el almacenamiento local.
     * Mapea a: GET /api/v1/guia/download/{id}
     *
     * @param solicitudId ID de la solicitud.
     * @param targetFile El objeto File donde se guardará el PDF (ej: File(context.filesDir, "guia_123.pdf")).
     * @return Result.Success con el objeto File si la descarga y escritura fueron exitosas, o Result.Failure.
     */
    suspend fun downloadGuidePdf(solicitudId: Long, targetFile: File): Result<File> {
        return try {
            // 1. Llamar al API para obtener el cuerpo de la respuesta binaria
            val response: Response<ResponseBody> = guideApi.downloadGuidePdf(solicitudId)

            if (response.isSuccessful) {
                val body: ResponseBody = response.body() ?: throw Exception("Cuerpo del PDF vacío.")

                // 2. Escribir el flujo de bytes en el archivo local
                writeResponseBodyToDisk(body, targetFile)

                // 3. Éxito
                Result.success(targetFile)
            } else {
                // Fallo de API: 4xx o 5xx (ej. 404 Not Found)
                val errorMessage = response.errorBody()?.string() ?: "Error al descargar la guía."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Fallo de red/escritura
            Result.failure(Exception("Error en la conexión o al guardar el archivo: ${e.message}"))
        }
    }

    /**
     * Función utilitaria para escribir el ResponseBody (InputStream) al disco.
     */
    private fun writeResponseBodyToDisk(body: ResponseBody, targetFile: File): Boolean {
        return try {
            val inputStream: InputStream = body.byteStream()
            val outputStream: FileOutputStream = FileOutputStream(targetFile)

            // Buffer para transferir el archivo eficientemente
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}