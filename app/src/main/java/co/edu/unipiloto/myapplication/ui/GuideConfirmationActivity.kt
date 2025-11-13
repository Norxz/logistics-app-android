package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import com.google.android.material.button.MaterialButton

/**
 * Activity que confirma la creación de una solicitud y muestra las opciones de guía.
 */
class GuideConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        val solicitudId = intent.getLongExtra("SOLICITUD_ID", -1L)

        initViews(solicitudId)
        setupListeners(solicitudId)
    }

    private fun initViews(solicitudId: Long) {
        // Se asume que tvStatusTitle y tvStatusMessage se actualizan dinámicamente
        findViewById<TextView>(R.id.tvStatusTitle).text = getString(R.string.guide_title_text)
        findViewById<TextView>(R.id.tvStatusMessage).text = getString(R.string.guide_description)
    }

    private fun setupListeners(solicitudId: Long) {
        findViewById<MaterialButton>(R.id.btnGenerateAndSend).setOnClickListener {
            // 💡 FUTURA IMPLEMENTACIÓN REST:
            // Aquí llamarías a un endpoint: POST /api/v1/guide/send/{solicitudId}
            Toast.makeText(this, "Generando y Enviando Guía $solicitudId...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnGenerateGuide).setOnClickListener {
            // 💡 FUTURA IMPLEMENTACIÓN REST:
            // Aquí llamarías a un endpoint: GET /api/v1/guide/download/{solicitudId}
            Toast.makeText(this, "Descargando Guía $solicitudId...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            // Regresar al dashboard principal
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}