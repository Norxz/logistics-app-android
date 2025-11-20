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
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.utils.FileUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * Activity que confirma la creación de una solicitud y muestra las opciones de guía.
 */
class GuideConfirmationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        sessionManager = SessionManager(this)

        val usuarioEmail = intent.getStringExtra("usuarioEmail")
        if (usuarioEmail.isNullOrEmpty()) {
            Toast.makeText(this, "No se recibió el email del usuario", Toast.LENGTH_LONG).show()
            return
        }

        val solicitudId = intent.getLongExtra("solicitudId", -1L)

        initViews()
        setupListeners(solicitudId, usuarioEmail)
    }

    private fun initViews() {
        findViewById<TextView>(R.id.tvStatusTitle).text = getString(R.string.guide_title_text)
        findViewById<TextView>(R.id.tvStatusMessage).text = getString(R.string.guide_description)
    }

    private fun setupListeners(solicitudId: Long, usuarioEmail: String) {
        findViewById<MaterialButton>(R.id.btnGenerateAndSend).setOnClickListener {
            Log.d("DEBUG_BTN", "SolicitudId: $solicitudId, UsuarioEmail: '$usuarioEmail'")
            if (solicitudId != -1L && usuarioEmail.isNotEmpty()) {
                downloadAndSendGuidePdf(solicitudId, usuarioEmail)
            } else {
                Toast.makeText(this, "ID de solicitud o correo no válido", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        findViewById<MaterialButton>(R.id.btnGenerateGuide).setOnClickListener {
            if (solicitudId != -1L) {
                downloadGuidePdf(solicitudId)
            } else {
                Toast.makeText(this, "ID de solicitud no válido", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
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

                    val pdfFile = withContext(Dispatchers.IO) {
                        val inputStream = response.body()!!.byteStream()

                        FileUtils.savePdfToExternalFile(
                            this@GuideConfirmationActivity,
                            inputStream,
                            "guia_solicitud_$solicitudId.pdf"
                        )
                    }

                    try {
                        FileUtils.openPdf(this@GuideConfirmationActivity, pdfFile)
                    } catch (_: ActivityNotFoundException) {
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
                Log.e("PDF_ERROR", "Error descargando PDF", e)
                Toast.makeText(
                    this@GuideConfirmationActivity,
                    "Error descargando PDF (ver LogCat)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun downloadAndSendGuidePdf(solicitudId: Long, usuarioEmail: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.downloadGuidePdf(solicitudId)
                }

                if (response.isSuccessful && response.body() != null) {

                    val pdfFile = withContext(Dispatchers.IO) {
                        val inputStream = response.body()!!.byteStream()
                        FileUtils.savePdfToExternalFile(
                            this@GuideConfirmationActivity,
                            inputStream,
                            "guia_solicitud_$solicitudId.pdf"
                        )
                    }

                    try {
                        FileUtils.openPdf(this@GuideConfirmationActivity, pdfFile)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@GuideConfirmationActivity,
                            "No hay visor de PDF instalado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    sendPdfByEmail(pdfFile, usuarioEmail)

                } else {
                    Toast.makeText(
                        this@GuideConfirmationActivity,
                        "Error al descargar PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("PDF_ERROR", "Error descargando PDF", e)
                Toast.makeText(
                    this@GuideConfirmationActivity,
                    "Error descargando PDF (ver LogCat)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sendPdfByEmail(pdfFile: File, usuarioEmail: String) {
        val uri = FileUtils.getUriForFile(this, pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(usuarioEmail))
            putExtra(Intent.EXTRA_SUBJECT, "Guía de Solicitud")
            putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás la guía de la solicitud.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar PDF por correo"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No hay app de correo instalada", Toast.LENGTH_SHORT).show()
        }
    }
}