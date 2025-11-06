package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import com.google.android.material.button.MaterialButton

/**
 * Activity que confirma la creación de una solicitud y muestra las opciones de guía.
 */
class GuideConfirmationActivity : AppCompatActivity() {

    /**
     * Initializes the activity: sets the layout and prepares UI and event handlers using the incoming solicitud ID.
     *
     * @param savedInstanceState Bundle containing activity state previously saved, or `null` if none.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        val solicitudId = intent.getLongExtra("SOLICITUD_ID", -1L)

        initViews(solicitudId)
        setupListeners(solicitudId)
    }

    /**
     * Initializes the status title and message views for the given request.
     *
     * @param solicitudId Identifier of the request whose status is being displayed.
    private fun initViews(solicitudId: Long) {
        // Se asume que tvStatusTitle y tvStatusMessage se actualizan dinámicamente
        findViewById<TextView>(R.id.tvStatusTitle).text = getString(R.string.guide_title_text)
        findViewById<TextView>(R.id.tvStatusMessage).text = getString(R.string.guide_description)
    }

    /**
     * Sets click listeners for the guide action buttons and the back navigation.
     *
     * - The "Generate and Send" button initiates generation and sending of the PDF guide and shows a status message including the given solicitudId.
     * - The "Generate Guide" button initiates downloading of the PDF guide and shows a status message including the given solicitudId.
     * - The "Back" button navigates to MainActivity and clears the activity stack.
     *
     * @param solicitudId Identifier of the request used in the guide actions and status messages.
     */
    private fun setupListeners(solicitudId: Long) {
        findViewById<MaterialButton>(R.id.btnGenerateAndSend).setOnClickListener {
            // TODO: Implementar lógica para generar la guía (PDF) y enviarla por email.
            Toast.makeText(this, "Generando y Enviando Guía $solicitudId...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnGenerateGuide).setOnClickListener {
            // TODO: Implementar lógica para solo descargar la guía (PDF).
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