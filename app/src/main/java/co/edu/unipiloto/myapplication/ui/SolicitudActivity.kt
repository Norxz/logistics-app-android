package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

/**
 * Activity para el registro de una nueva solicitud de envío.
 */
class SolicitudActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var solicitudRepository: SolicitudRepository

    // Vistas principales (remitente y paquete)
    private lateinit var etSenderName: TextInputEditText
    private lateinit var etSenderID: TextInputEditText
    private lateinit var spIDType: Spinner
    private lateinit var etReceiverAddress: TextInputEditText
    private lateinit var btnSend: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_delivery)

        sessionManager = SessionManager(this)
        solicitudRepository = SolicitudRepository(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getUserId() == -1L) {
            Toast.makeText(this, "Debe iniciar sesión para crear una solicitud.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        etSenderName = findViewById(R.id.etSenderName)
        etSenderID = findViewById(R.id.etSenderID)
        spIDType = findViewById(R.id.spIDType)
        etReceiverAddress = findViewById(R.id.etReceiverAddress)
        btnSend = findViewById(R.id.btnSend)

        // Asumiendo que etReceiverAddress es el campo de la dirección de recogida final.
        // Si tienes una dirección previa de PickUpLocationActivity, puedes cargarla aquí.
        // val pickupAddress = intent.getStringExtra("PICKUP_ADDRESS") ?: ""
        // etReceiverAddress.setText(pickupAddress)
    }

    private fun setupSpinners() {
        // Spinner Tipo de ID
        val idTypes = listOf("Cédula", "Pasaporte", "RUT")
        spIDType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, idTypes)

        // Spinner Código de País (Remitente y Destinatario)
        val countryCodes = listOf("+57", "+1", "+52")
        val codeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)

        findViewById<Spinner>(R.id.spSenderCountryCode).adapter = codeAdapter
        findViewById<Spinner>(R.id.spReceiverCountryCode).adapter = codeAdapter

        // TODO: Configurar un spinner para seleccionar la Zona/Ciudad de recogida (necesaria para la BD)
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            submitSolicitud()
        }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.logoutUser()
            finish()
        }
    }

    private fun submitSolicitud() {
        // 1. Recopilar y validar datos
        val address = etReceiverAddress.text.toString().trim()
        val weight = findViewById<TextInputEditText>(R.id.etPackageWeight).text.toString().toDoubleOrNull()
        val price = findViewById<TextInputEditText>(R.id.etPrice).text.toString().toDoubleOrNull()
        val content = findViewById<TextInputEditText>(R.id.etPackageContent).text.toString().trim()

        if (address.isEmpty() || weight == null || price == null || content.isEmpty()) {
            Toast.makeText(this, "Faltan campos obligatorios de envío.", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Definir fecha, franja y zona (TEMPORAL: Debes obtenerlos de la UI)
        val today = Calendar.getInstance().get(Calendar.YEAR).toString() + "-" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val franja = "AM" // Ejemplo, obtener de un spinner
        val zona = "Bogotá - Norte" // Ejemplo, obtener de la UI

        // 3. Crear solicitud
        val newId = solicitudRepository.crear(
            userId = sessionManager.getUserId(),
            direccionCompleta = address,
            fechaRecoleccion = today,
            franjaHoraria = franja,
            notas = content, // Usamos contenido como nota de ejemplo
            zona = zona
        )

        if (newId != -1L) {
            Toast.makeText(this, "Solicitud $newId creada exitosamente!", Toast.LENGTH_LONG).show()

            // Redirigir a la pantalla de éxito/guía
            val intent = Intent(this, GuideConfirmationActivity::class.java).apply {
                putExtra("SOLICITUD_ID", newId)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error al crear la solicitud. Intente de nuevo.", Toast.LENGTH_LONG).show()
        }
    }
}