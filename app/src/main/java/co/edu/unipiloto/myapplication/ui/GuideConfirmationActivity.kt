package co.edu.unipiloto.myapplication.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.utils.FileUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Activity que confirma la creación de una solicitud y muestra las opciones de guía.
 */
class GuideConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        val solicitudId = intent.getLongExtra("SOLICITUD_ID", -1L)

        initViews()
        setupListeners(solicitudId)
    }

    private fun initViews() {
        // Se asume que tvStatusTitle y tvStatusMessage se actualizan dinámicamente
        findViewById<TextView>(R.id.tvStatusTitle).text = getString(R.string.guide_title_text)
        findViewById<TextView>(R.id.tvStatusMessage).text = getString(R.string.guide_description)
    }

    private fun setupListeners(solicitudId: Long) {
        findViewById<MaterialButton>(R.id.btnGenerateAndSend).setOnClickListener {
            // 💡 FUTURA IMPLEMENTACIÓN REST:
            // Aquí llamarías a un endpoint: POST /api/v1/guide/send/{solicitudId}
            Toast.makeText(this, "Generando y Enviando Guía $solicitudId...", Toast.LENGTH_SHORT)
                .show()
        }

        findViewById<MaterialButton>(R.id.btnGenerateGuide).setOnClickListener {
            if (solicitudId != -1L) {
                downloadGuidePdf(solicitudId)
            } else {
                Toast.makeText(this, "ID de solicitud no válido", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            // Regresar al dashboard principal
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun downloadGuidePdf(solicitudId: Long) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.downloadGuidePdf(solicitudId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val inputStream = response.body()!!.byteStream()

                    val pdfFile = FileUtils.savePdfToExternalFile(
                        this@GuideConfirmationActivity,
                        inputStream,
                        "guia_solicitud_$solicitudId.pdf"
                    )

                    try {
                        FileUtils.openPdf(this@GuideConfirmationActivity, pdfFile)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@GuideConfirmationActivity,
                            "No hay visor de PDF instalado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@GuideConfirmationActivity,
                        "Error al descargar PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@GuideConfirmationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}