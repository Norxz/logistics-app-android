package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.rest.SolicitudRequest
import co.edu.unipiloto.myapplication.models.Solicitud
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

/**
 * Activity para el registro de una nueva solicitud de envío.
 */
class SolicitudActivity : AppCompatActivity() {

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager

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

    // --- VISTAS DESTINATARIO (ACTUALIZADAS PARA ICONOS) ---
    private lateinit var etReceiverName: TextInputEditText
    private lateinit var etReceiverPhone: TextInputEditText
    private lateinit var spReceiverCountryCode: Spinner

    // Referencias a los EditTexts envueltos:
    private lateinit var etReceiverID: TextInputEditText
    private lateinit var etReceiverAddress: TextInputEditText

    // Referencias a los Contenedores (TextInputLayout) para los End Icons:
    private lateinit var tilReceiverID: TextInputLayout
    private lateinit var tilReceiverAddress: TextInputLayout

    // --- VISTAS RECOLECCIÓN Y PRECIO ---
    private lateinit var spCiudad: Spinner // ID: spCity
    private lateinit var spFranja: Spinner // ID: spTimeSlot
    private lateinit var etPrice: TextInputEditText

    // --- ACCIÓN ---
    private lateinit var btnSend: MaterialButton


    // --- DATOS DE RECOLECCIÓN ---
    private val ZONAS_DISPONIBLES = listOf("Bogotá - Norte", "Bogotá - Sur", "Bogotá - Occidente")
    private val FRANJAS_HORARIAS = listOf("AM (8:00 - 12:00)", "PM (14:00 - 18:00)")

    private var recolectionAddress: String? = null
    private var recolectionLatitude: Double? = null
    private var recolectionLongitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.new_shipment_title)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getUserId() == -1L) {
            Toast.makeText(this, "Debe iniciar sesión para crear una solicitud.", Toast.LENGTH_LONG)
                .show()
            finish()
            return
        }

        initViews()
        setupSpinners()
        handleIntentData()
        setupListeners()
    }
    // -----------------------------------------------------------------------------------
    private fun handleIntentData() {
        recolectionAddress = intent.getStringExtra("RECOLECTION_ADDRESS")
        recolectionLatitude = intent.getDoubleExtra("RECOLECTION_LATITUDE", Double.NaN).let {
            if (it.isNaN()) null else it
        }
        recolectionLongitude = intent.getDoubleExtra("RECOLECTION_LONGITUDE", Double.NaN).let {
            if (it.isNaN()) null else it
        }

        if (!recolectionAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Dirección de recolección recibida: $recolectionAddress", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViews() {
        // --- VISTAS REMITENTE ---
        etSenderName = findViewById(R.id.etSenderName)
        etSenderID = findViewById(R.id.etSenderID)
        etSenderPhone = findViewById(R.id.etSenderPhone)
        spIDType = findViewById(R.id.spIDType)
        spSenderCountryCode = findViewById(R.id.spSenderCountryCode)

        // --- VISTAS PAQUETE ---
        etPackageHeight = findViewById(R.id.etPackageHeight)
        etPackageWidth = findViewById(R.id.etPackageWidth)
        etPackageLength = findViewById(R.id.etPackageLength)
        etPackageWeight = findViewById(R.id.etPackageWeight)
        etPackageContent = findViewById(R.id.etPackageContent)

        // --- VISTAS DESTINATARIO ---
        etReceiverName = findViewById(R.id.etReceiverName)
        etReceiverPhone = findViewById(R.id.etReceiverPhone)
        spReceiverCountryCode = findViewById(R.id.spReceiverCountryCode)

        // Buscar EditTexts
        etReceiverAddress = findViewById(R.id.etReceiverAddress)
        etReceiverID = findViewById(R.id.etReceiverID)

        // Obtener las referencias de TextInputLayout (el padre directo del EditText)
        // Esto es necesario para configurar los click listeners en los iconos (endIconMode)
        tilReceiverID = etReceiverID.parent as TextInputLayout
        tilReceiverAddress = etReceiverAddress.parent as TextInputLayout

        // --- VISTAS RECOLECCIÓN Y PRECIO ---
        spCiudad = findViewById(R.id.spCity)
        spFranja = findViewById(R.id.spTimeSlot)
        etPrice = findViewById(R.id.etPrice)

        // --- ACCIÓN ---
        btnSend = findViewById(R.id.btnSend)
    }

    private fun setupSpinners() {
        val idTypes = listOf("Cédula", "Pasaporte", "RUT")
        spIDType.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, idTypes)

        val countryCodes = listOf("+57", "+1", "+52")
        val codeAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)

        spSenderCountryCode.adapter = codeAdapter
        spReceiverCountryCode.adapter = codeAdapter

        spCiudad.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ZONAS_DISPONIBLES)

        spFranja.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, FRANJAS_HORARIAS)
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            submitSolicitud()
        }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.logoutUser()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // --- Listener para el ícono de Ubicación (etReceiverAddress) ---
        tilReceiverAddress.setEndIconOnClickListener {
            Toast.makeText(this, "Abriendo mapa para seleccionar la dirección de entrega...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para iniciar una actividad de selección de mapa.
            // Por ejemplo: startActivity(Intent(this, MapSelectionActivity::class.java).apply { putExtra("MODE", "DESTINATION") })
        }

        // --- Listener para el ícono de Lupa (etReceiverID) ---
        tilReceiverID.setEndIconOnClickListener {
            val receiverID = etReceiverID.text.toString().trim()
            if (receiverID.isNotEmpty()) {
                Toast.makeText(this, "Simulando búsqueda de destinatario con ID: $receiverID", Toast.LENGTH_SHORT).show()
                // Aquí iría la lógica REST de búsqueda de cliente/destinatario.
            } else {
                Toast.makeText(this, "Ingrese una identificación para buscar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitSolicitud() {
        // 1. Recopilar y validar datos
        val senderName = etSenderName.text.toString().trim()
        val senderID = etSenderID.text.toString().trim()
        val senderPhone = etSenderPhone.text.toString().trim()
        val receiverName = etReceiverName.text.toString().trim()
        val receiverPhone = etReceiverPhone.text.toString().trim()
        val receiverAddress = etReceiverAddress.text.toString().trim()
        val weight = etPackageWeight.text.toString().toDoubleOrNull()
        val content = etPackageContent.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull()
        val zonaCompleta = spCiudad.selectedItem?.toString()?.trim() ?: ""
        val franja = spFranja.selectedItem?.toString()?.trim() ?: ""
        val recolectionDir = recolectionAddress ?: ""
        val recolectionLat = recolectionLatitude
        val recolectionLon = recolectionLongitude
        val ciudad = zonaCompleta.split(" - ").firstOrNull() ?: ""

        // 2. Validación
        if (!validateFields(
                senderName, senderID, senderPhone,
                receiverName, receiverPhone, receiverAddress,
                weight, content, price,
                zonaCompleta, franja
            )
        ) {
            return
        }

        if (recolectionDir.isEmpty() || recolectionLat == null || recolectionLon == null) {
            Toast.makeText(this, "Faltan los datos de la dirección de Recolección.", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Definir fecha de recolección
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val today = "$year-$month-$day"

        // 4. 🌟 CREAR REQUEST DTO para el Backend 🌟
        val solicitudRequest = SolicitudRequest(
            clientId = sessionManager.getUserId(),
            direccionCompleta = recolectionDir,
            ciudad = ciudad,
            latitud = recolectionLat,
            longitud = recolectionLon,
            pesoKg = weight!!,
            precio = price!!,
            fechaRecoleccion = today,
            franjaHoraria = franja,
            notas = content,
            zona = zonaCompleta,
            pisoApto = null,
            notasEntrega = null
        )

        // 5. Llamar al servicio REST (Reemplazando la llamada a SQLite)
        RetrofitClient.apiService.crearSolicitud(solicitudRequest).enqueue(object : Callback<Solicitud> {
            override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {
                if (response.isSuccessful && response.body() != null) {
                    val nuevaSolicitud = response.body()!!

                    Toast.makeText(this@SolicitudActivity, "Solicitud ${nuevaSolicitud.id} creada exitosamente!", Toast.LENGTH_LONG).show()

                    // 6. Redirigir a la pantalla de éxito/guía
                    val intent = Intent(this@SolicitudActivity, GuideConfirmationActivity::class.java).apply {
                        putExtra("SOLICITUD_ID", nuevaSolicitud.id)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    Log.e("Solicitud", "Error ${response.code()}: $errorBody")
                    Toast.makeText(this@SolicitudActivity, "Error al crear solicitud. Intente de nuevo.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                Log.e("Solicitud", "Fallo de red: ${t.message}")
                Toast.makeText(this@SolicitudActivity, "Fallo de red al enviar solicitud.", Toast.LENGTH_LONG).show()
            }
        })
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

        fun clearError(editText: TextInputEditText) {
            // El padre directo del EditText es el TextInputLayout
            (editText.parent as? TextInputLayout)?.error = null
        }

        fun setError(editText: TextInputEditText, message: String) {
            (editText.parent as? TextInputLayout)?.error = message
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

        // Validación de Precio
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