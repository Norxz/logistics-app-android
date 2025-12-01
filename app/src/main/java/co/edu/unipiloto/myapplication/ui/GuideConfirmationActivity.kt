package co.edu.unipiloto.myapplication.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.utils.FileUtils
import co.edu.unipiloto.myapplication.utils.NotificationHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
// 🚨 IMPORTACIÓN CRUCIAL: Necesaria para usar .isSuccessful, .body(), etc.
import retrofit2.Response

/**
 * Activity que confirma la creación de una solicitud y muestra las opciones de guía.
 * Gestiona la descarga local de la guía PDF y simula la solicitud de envío por correo.
 */
class GuideConfirmationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var solicitudId: Long = -1L
    private var usuarioEmail: String? = null
    private val solicitudApiService by lazy { RetrofitClient.getSolicitudApi() }
    private val guideApiService by lazy { RetrofitClient.getGuideApi() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        sessionManager = SessionManager(this)

        solicitudId = intent.getLongExtra("solicitudId", -1L)
        usuarioEmail = intent.getStringExtra("usuarioEmail")

        if (usuarioEmail.isNullOrEmpty() || solicitudId == -1L) {
            Toast.makeText(this, "Error: Datos de solicitud incompletos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Asumiendo que el layout usa los IDs R.id.tvStatusTitle y R.id.tvStatusMessage
        findViewById<TextView>(R.id.tvStatusTitle).text = getString(R.string.guide_title_text)
        findViewById<TextView>(R.id.tvStatusMessage).text = getString(R.string.guide_description)
    }

    private fun setupListeners() {
        // 1. Botón de DESCARGAR Y ENVIAR POR CORREO (Llamada al backend)
        findViewById<MaterialButton>(R.id.btnGenerateAndSend).setOnClickListener {
            simulateSendGuideEmail()
        }

        // 2. Botón de DESCARGAR (Almacenamiento local)
        findViewById<MaterialButton>(R.id.btnGenerateGuide).setOnClickListener {
            downloadAndOpenGuidePdf(solicitudId)
        }

        // 3. Botón de VOLVER
        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            // Regresar al MainActivity o al Dashboard
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Lógica unificada para descargar el PDF desde la API y abrirlo con una aplicación externa.
     * @param solicitudId ID de la solicitud a descargar.
     * @param showSuccessToast Indica si se debe mostrar un Toast de éxito.
     */
    private fun downloadAndOpenGuidePdf(solicitudId: Long, showSuccessToast: Boolean = true) {

        lifecycleScope.launch {
            var pdfFile: File? = null
            try {
                // Se ejecuta la llamada de red en un hilo de IO
                val response = withContext(Dispatchers.IO) {
                    // 🚨 CORRECCIÓN: Cambiar guideApiService por solicitudApiService
                    solicitudApiService.generarPdf(solicitudId)
                }

                // 🚨 CORRECCIÓN 2: 'response.isSuccessful' y 'response.body()' ahora funcionan gracias al import 'retrofit2.Response'
                if (response.isSuccessful && response.body() != null) {
                    // Guarda el archivo en almacenamiento externo (Downloads)
                    pdfFile = withContext(Dispatchers.IO) {
                        val inputStream = response.body()!!.byteStream()
                        FileUtils.savePdfToExternalFile(
                            this@GuideConfirmationActivity,
                            inputStream,
                            "guia_solicitud_$solicitudId.pdf"
                        )
                    }

                    if (pdfFile != null) {
                        try {
                            FileUtils.openPdf(this@GuideConfirmationActivity, pdfFile!!)
                            if (showSuccessToast) {
                                Toast.makeText(
                                    this@GuideConfirmationActivity,
                                    "Guía descargada en la carpeta Downloads",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                this@GuideConfirmationActivity,
                                "No hay visor de PDF instalado. Archivo guardado.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    // 🚨 CORRECCIÓN 3: Quitar el paréntesis extra 'response.code()' -> 'response.code'
                    Toast.makeText(
                        this@GuideConfirmationActivity,
                        "Error ${response.code()} al descargar PDF", // Usar .code (Int)
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("PDF_ERROR", "Error descargando PDF", e)
                Toast.makeText(
                    this@GuideConfirmationActivity,
                    "Error de conexión al descargar PDF.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Simula el proceso de solicitud de envío de correo, notificando al usuario.
     */
    private fun simulateSendGuideEmail() {
        Toast.makeText(
            this,
            "Procesando envío de guía al correo ${usuarioEmail}. Esto debe ser manejado por el servidor.",
            Toast.LENGTH_LONG
        ).show()

        // 1. Descargar el archivo primero (opcional, para visualización inmediata)
        downloadAndOpenGuidePdf(solicitudId, showSuccessToast = false)

        // 2. Simular el envío exitoso de la notificación (como si el backend hubiera respondido OK)
        NotificationHelper.showNotification(
            context = this,
            title = "Envío de Guía Solicitado",
            content = "El correo con la guía de la solicitud #$solicitudId está siendo procesado y se enviará a ${usuarioEmail}"
        )
    }
}