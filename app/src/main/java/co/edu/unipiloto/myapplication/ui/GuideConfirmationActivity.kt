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
import java.util.Properties
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart


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
                // Mostrar diálogo de progreso
                val progressDialog =
                    android.app.ProgressDialog(this@GuideConfirmationActivity).apply {
                        setMessage("Descargando y enviando guía...")
                        setCancelable(false)
                        show()
                    }

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

                    // Enviar el PDF por correo en segundo plano
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val senderEmail = "santi.ch.lokcis@gmail.com"
                            val senderPassword = "ubwd edem tnfc uktb" // App Password

                            val messageBody = """
                            Hola,

                            Adjunto encontrarás la guía de la solicitud #$solicitudId.

                            Saludos,
                            Logistics App
                        """.trimIndent()

                            sendMailWithAttachment(
                                fromEmail = senderEmail,
                                fromPassword = senderPassword,
                                toEmail = usuarioEmail,
                                subject = "Guía de Solicitud",
                                messageBody = messageBody,
                                pdfFile = pdfFile
                            )

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@GuideConfirmationActivity,
                                    "Correo enviado correctamente a $usuarioEmail",
                                    Toast.LENGTH_LONG
                                ).show()

                                NotificationHelper.showNotification(
                                    context = this@GuideConfirmationActivity,
                                    title = "Guía enviada",
                                    content = "El correo con la guía de la solicitud #$solicitudId fue enviado a $usuarioEmail"
                                )
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@GuideConfirmationActivity,
                                    "Error enviando correo: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } finally {
                            withContext(Dispatchers.Main) { progressDialog.dismiss() }
                        }
                    }

                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@GuideConfirmationActivity,
                        "Error al descargar PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@GuideConfirmationActivity,
                    "Error descargando PDF (ver LogCat)",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("PDF_ERROR", "Error descargando PDF", e)
            }
        }
    }


    private fun sendPdfByEmail(pdfFile: File, usuarioEmail: String) {
        val senderEmail = "santi.ch.lokcis@gmail.com"
        val senderPassword = "ubwd edem tnfc uktb" // ⚠️ Genera un App Password en Gmail

        val messageBody = """
        Hola,
        
        Adjunto encontrarás la guía de la solicitud #${intent.getLongExtra("solicitudId", -1L)}.
        
        Saludos,
        Logistics App
    """.trimIndent()

        sendMailWithAttachment(
            fromEmail = senderEmail,
            fromPassword = senderPassword,
            toEmail = usuarioEmail,
            subject = "Guía de Solicitud",
            messageBody = messageBody,
            pdfFile = pdfFile
        )

        Toast.makeText(this, "Correo enviado en segundo plano a $usuarioEmail", Toast.LENGTH_LONG)
            .show()
    }

    fun sendMailWithAttachment(
        fromEmail: String,
        fromPassword: String,
        toEmail: String,
        subject: String,
        messageBody: String,
        pdfFile: File
    ) {
        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(fromEmail, fromPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(fromEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                    setSubject(subject)
                }

                val multipart = MimeMultipart()

                // Texto
                val textPart = MimeBodyPart()
                textPart.setText(messageBody)
                multipart.addBodyPart(textPart)

                // PDF
                val filePart = MimeBodyPart()
                filePart.attachFile(pdfFile)
                multipart.addBodyPart(filePart)

                message.setContent(multipart)

                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

}