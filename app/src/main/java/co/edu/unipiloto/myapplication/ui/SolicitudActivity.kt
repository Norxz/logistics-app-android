package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

/**
 * Activity para el registro de una nueva solicitud de envío.
 * Usa el layout activity_new_delivery.xml proporcionado.
 */
class SolicitudActivity : AppCompatActivity() {

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager
    private lateinit var solicitudRepository: SolicitudRepository

    // --- VISTAS REMITENTE ---
    private lateinit var etSenderName: TextInputEditText
    private lateinit var etSenderID: TextInputEditText
    private lateinit var etSenderPhone: TextInputEditText
    private lateinit var spIDType: Spinner
    private lateinit var spSenderCountryCode: Spinner

    // --- VISTAS PAQUETE (Dimensiones y Contenido) ---
    private lateinit var etPackageHeight: TextInputEditText
    private lateinit var etPackageWidth: TextInputEditText
    private lateinit var etPackageLength: TextInputEditText
    private lateinit var etPackageWeight: TextInputEditText
    private lateinit var etPackageContent: TextInputEditText

    // --- VISTAS DESTINATARIO ---
    private lateinit var etReceiverName: TextInputEditText
    private lateinit var etReceiverPhone: TextInputEditText
    private lateinit var etReceiverAddress: TextInputEditText
    private lateinit var spReceiverCountryCode: Spinner

    // --- VISTAS RECOLECCIÓN Y PRECIO ---
    private lateinit var spCiudad: Spinner // ID: spCity
    private lateinit var spFranja: Spinner // ID: spTimeSlot
    private lateinit var etPrice: TextInputEditText

    // --- ACCIÓN ---
    private lateinit var btnSend: MaterialButton


    // --- DATOS ---
    private val ZONAS_DISPONIBLES = listOf("Bogotá - Norte", "Bogotá - Sur", "Bogotá - Occidente")
    private val FRANJAS_HORARIAS = listOf("AM (8:00 - 12:00)", "PM (14:00 - 18:00)")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.new_shipment_title)

        sessionManager = SessionManager(this)
        solicitudRepository = SolicitudRepository(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getUserId() == -1L) {
            Toast.makeText(this, "Debe iniciar sesión para crear una solicitud.", Toast.LENGTH_LONG)
                .show()
            finish()
            return
        }

        initViews()
        setupSpinners()
        setupListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViews() {
        // --- Remitente ---
        etSenderName = findViewById(R.id.etSenderName)
        etSenderID = findViewById(R.id.etSenderID)
        etSenderPhone = findViewById(R.id.etSenderPhone)
        spIDType = findViewById(R.id.spIDType)
        spSenderCountryCode = findViewById(R.id.spSenderCountryCode)

        // --- Paquete ---
        etPackageHeight = findViewById(R.id.etPackageHeight)
        etPackageWidth = findViewById(R.id.etPackageWidth)
        etPackageLength = findViewById(R.id.etPackageLength)
        etPackageWeight = findViewById(R.id.etPackageWeight)
        etPackageContent = findViewById(R.id.etPackageContent)

        // --- Destinatario ---
        etReceiverName = findViewById(R.id.etReceiverName)
        etReceiverPhone = findViewById(R.id.etReceiverPhone)
        spReceiverCountryCode = findViewById(R.id.spReceiverCountryCode)
        etReceiverAddress = findViewById(R.id.etReceiverAddress)

        // --- Recolección y Precio ---
        spCiudad = findViewById(R.id.spCity)
        spFranja = findViewById(R.id.spTimeSlot)
        etPrice = findViewById(R.id.etPrice)

        // --- Acción ---
        btnSend = findViewById(R.id.btnSend)
    }

    private fun setupSpinners() {
        // Spinner Tipo de ID
        val idTypes = listOf("Cédula", "Pasaporte", "RUT")
        spIDType.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, idTypes)

        // Spinner Código de País (Remitente y Destinatario)
        val countryCodes = listOf("+57", "+1", "+52")
        val codeAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)

        spSenderCountryCode.adapter = codeAdapter
        spReceiverCountryCode.adapter = codeAdapter

        // Configurar Spinner de Ciudad/Zona
        spCiudad.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ZONAS_DISPONIBLES)

        // Configurar Spinner de Franja Horaria
        spFranja.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, FRANJAS_HORARIAS)
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            submitSolicitud()
        }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.logoutUser()
            // Redirigir a Login y limpiar la pila de actividades
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun submitSolicitud() {
        // 1. Recopilar y validar datos

        // 1.1 Remitente
        val senderName = etSenderName.text.toString().trim()
        val senderID = etSenderID.text.toString().trim()
        val senderPhone = etSenderPhone.text.toString().trim()

        // 1.2 Destinatario
        val receiverName = etReceiverName.text.toString().trim()
        val receiverPhone = etReceiverPhone.text.toString().trim()
        val receiverAddress = etReceiverAddress.text.toString().trim()

        // 1.3 Paquete y Costo
        val weight = etPackageWeight.text.toString().toDoubleOrNull()
        val content = etPackageContent.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull()

        // 1.4 Recolección
        val zonaCompleta = spCiudad.selectedItem?.toString()?.trim() ?: ""
        val franja = spFranja.selectedItem?.toString()?.trim() ?: ""

        // Extraer la Ciudad (e.g., "Bogotá - Norte" -> "Bogotá")
        val ciudad = zonaCompleta.split(" - ").firstOrNull() ?: ""

        // 2. Validación
        if (!validateFields(
                senderName, senderID, senderPhone,
                receiverName, receiverPhone, receiverAddress,
                weight, content, price,
                zonaCompleta, franja
            )
        ) {
            return // La validación muestra el Toast o error
        }

        // 3. Definir fecha de recolección (Hoy en formato yyyy-mm-dd)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val today = "$year-$month-$day"

        // 4. Crear solicitud en la BD
        val newId = solicitudRepository.crearSolicitud(
            userId = sessionManager.getUserId(),
            direccionCompleta = receiverAddress,
            ciudad = ciudad,
            peso = weight!!,
            precio = price!!,
            fechaRecoleccion = today,
            franjaHoraria = franja,
            notas = content,
            zona = zonaCompleta
        )

        if (newId != -1L) {
            Toast.makeText(this, "Solicitud $newId creada exitosamente!", Toast.LENGTH_LONG).show()

            // 5. Redirigir a la pantalla de éxito/guía
            val intent = Intent(this, GuideConfirmationActivity::class.java).apply {
                putExtra("SOLICITUD_ID", newId)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(
                this,
                "Error al crear la solicitud. Intente de nuevo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Función que valida todos los campos y muestra los errores directamente en el TextInputLayout.
     */
    private fun validateFields(
        senderName: String, senderID: String, senderPhone: String,
        receiverName: String, receiverPhone: String, receiverAddress: String,
        weight: Double?, content: String, price: Double?,
        zonaCompleta: String, franja: String
    ): Boolean {

        var isValid = true

        // Limpiar errores (No podemos usar til.error = null porque no tienen ID, así que usamos el parent)
        fun clearError(editText: TextInputEditText) {
            (editText.parent.parent as? TextInputLayout)?.error = null
        }

        // Función para establecer error
        fun setError(editText: TextInputEditText, message: String) {
            (editText.parent.parent as? TextInputLayout)?.error = message
            isValid = false
        }

        clearError(etSenderName)
        clearError(etSenderID)
        clearError(etSenderPhone)
        clearError(etReceiverName)
        clearError(etReceiverPhone)
        clearError(etReceiverAddress)
        clearError(etPackageWeight)
        clearError(etPackageContent)
        clearError(etPrice)

        // Validación de Remitente
        if (senderName.isEmpty()) setError(etSenderName, "Nombre obligatorio")
        if (senderID.isEmpty()) setError(etSenderID, "Identificación obligatoria")
        if (senderPhone.isEmpty() || senderPhone.length < 7) setError(
            etSenderPhone,
            "Teléfono inválido"
        )

        // Validación de Destinatario
        if (receiverName.isEmpty()) setError(etReceiverName, "Nombre obligatorio")
        if (receiverPhone.isEmpty() || receiverPhone.length < 7) setError(
            etReceiverPhone,
            "Teléfono inválido"
        )
        if (receiverAddress.isEmpty()) setError(
            etReceiverAddress,
            "Dirección de entrega obligatoria"
        )

        // Validación de Paquete
        if (weight == null || weight <= 0) setError(etPackageWeight, "Peso inválido")
        if (content.isEmpty()) setError(etPackageContent, "Contenido obligatorio")

        // Validación de Precio (Aunque en un entorno real se calcularía)
        if (price == null || price <= 0) setError(etPrice, "Precio inválido")

        // Validación de Recolección (Spinner)
        if (zonaCompleta.isEmpty() || franja.isEmpty()) {
            Toast.makeText(
                this,
                "Debe seleccionar la Zona y Franja Horaria de recolección.",
                Toast.LENGTH_LONG
            ).show()
            isValid = false
        }

        return isValid
    }
}