package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.*
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.utils.LocationHelper
import com.google.android.gms.maps.MapView
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.SucursalRepository
// 🎯 CORRECCIÓN: Usamos las clases renombradas para la creación (NewRequestCreation...)
import co.edu.unipiloto.myapplication.viewmodel.NewRequestCreationViewModel
import co.edu.unipiloto.myapplication.viewmodel.NewRequestCreationVMFactory


class SolicitudActivity : AppCompatActivity() {

    // ... (Variables de Vistas)
    private lateinit var etSenderName: TextInputEditText
    private lateinit var etSenderID: TextInputEditText
    private lateinit var etSenderPhone: TextInputEditText
    private lateinit var spIDType: Spinner
    private lateinit var spSenderCountryCode: Spinner
    private lateinit var etPackageHeight: TextInputEditText
    private lateinit var etPackageWidth: TextInputEditText
    private lateinit var etPackageLength: TextInputEditText
    private lateinit var etPackageWeight: TextInputEditText
    private lateinit var etPackageContent: TextInputEditText
    private lateinit var spReceiverIDType: Spinner
    private lateinit var etReceiverID: TextInputEditText
    private lateinit var etReceiverName: TextInputEditText
    private lateinit var etReceiverPhone: TextInputEditText
    private lateinit var etReceiverAddress: TextInputEditText
    private lateinit var spReceiverCountryCode: Spinner
    private lateinit var tilReceiverID: TextInputLayout
    private lateinit var btnReceiverGps: ImageButton
    private lateinit var tvRecolectionAddress: TextView
    private lateinit var spFranja: Spinner
    private lateinit var etPrice: TextInputEditText
    private lateinit var btnSend: View
    private var recoleccionAddress: String? = null
    private var recoleccionLat: Double? = null
    private var recoleccionLon: Double? = null
    private var entregaLat: Double? = null
    private var entregaLon: Double? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var locationHelper: LocationHelper

    // 🌟 CORRECCIÓN: Usamos el ViewModel renombrado
    private lateinit var viewModel: NewRequestCreationViewModel
    // -------------------------------------------------------------------

    private val handler = Handler(Looper.getMainLooper())
    private var priceUpdateRunnable: Runnable? = null
    // -------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud)

        sessionManager = SessionManager(this)

        initViews()
        setupSpinners()
        setupEndIcons()
        processRecolectionIntent(intent)
        setupViewModel()
        setupObservers()

        // CONFIGURACIÓN DEL MAPA Y GEOLOCALIZACIÓN PARA ENTREGA
        val mapView = findViewById<MapView>(R.id.mapViewReceiver)
        mapView.onCreate(null)

        locationHelper = LocationHelper(
            this,
            mapView
        ) { address, lat, lon, city ->
            etReceiverAddress.setText(address)
            entregaLat = lat
            entregaLon = lon
            calculatePrice()
        }

        btnReceiverGps.setOnClickListener {
            // Re-inicializa LocationHelper con el callback para la ubicación actual
            locationHelper = LocationHelper(
                this,
                mapView
            ) { address, lat, lon, city ->
                etReceiverAddress.setText(address)
                entregaLat = lat
                entregaLon = lon
                calculatePrice()
            }
        }

        etReceiverAddress.setOnEditorActionListener { _, actionId, event ->
            val isEnter = actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.action == android.view.KeyEvent.ACTION_DOWN)

            if (isEnter) {
                val query = etReceiverAddress.text.toString().trim()
                if (query.isNotEmpty()) locationHelper.searchAddress(query)
                true
            } else false
        }

        setupListeners()
    }

    // --- SECCIÓN DE VIEWMODEL ---

    private fun setupViewModel() {
        // Asumiendo que RetrofitClient y Repositories existen (usando RetrofitClient.solicitudService/sucursalService)
        val solicitudRepo = SolicitudRepository(RetrofitClient.getSolicitudApi())
        val sucursalRepo = SucursalRepository(RetrofitClient.getSucursalApi())

        // ✅ CORRECCIÓN: Usamos la Factory que acepta dos repositorios
        val factory = NewRequestCreationVMFactory(solicitudRepo, sucursalRepo)

        // ✅ CORRECCIÓN: Usamos el ViewModel renombrado
        viewModel = ViewModelProvider(this, factory)[NewRequestCreationViewModel::class.java]
    }

    private fun setupObservers() {
        // ✅ CORRECCIÓN: Observar el LiveData 'saveResult' del nuevo ViewModel
        viewModel.saveResult.observe(this) { result ->
            result.onSuccess { response: SolicitudResponse ->
                val solicitudId = response.id

                Toast.makeText(
                    this,
                    "¡Solicitud enviada (Guía #${solicitudId})!",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, GuideConfirmationActivity::class.java)
                intent.putExtra("solicitudId", solicitudId)
                intent.putExtra("usuarioEmail", sessionManager.getUserEmail())
                startActivity(intent)
                finish()
            }.onFailure { exception: Throwable ->
                Log.e("API_CALL", "Error al procesar solicitud: ${exception.message}")
                Toast.makeText(
                    this,
                    "Error al enviar solicitud: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ----------------------------------


    private fun processRecolectionIntent(intent: Intent) {
        recoleccionAddress = intent.getStringExtra("RECOLECTION_ADDRESS")
        recoleccionLat = intent.getDoubleExtra("RECOLECTION_LATITUDE", 0.0).takeIf { it != 0.0 }
        recoleccionLon = intent.getDoubleExtra("RECOLECTION_LONGITUDE", 0.0).takeIf { it != 0.0 }

        tvRecolectionAddress.text = recoleccionAddress ?: "Dirección de Recolección no definida"

        if (recoleccionAddress == null || recoleccionLat == null || recoleccionLon == null) {
            Toast.makeText(this, "Error: Datos de Recolección incompletos.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        etSenderName = findViewById(R.id.etSenderName)
        etSenderID = findViewById(R.id.etSenderID)
        etSenderPhone = findViewById(R.id.etSenderPhone)
        spIDType = findViewById(R.id.spSenderIDType)
        spSenderCountryCode = findViewById(R.id.spSenderCountryCode)
        etPackageHeight = findViewById(R.id.etPackageHeight)
        etPackageWidth = findViewById(R.id.etPackageWidth)
        etPackageLength = findViewById(R.id.etPackageLength)
        etPackageWeight = findViewById(R.id.etPackageWeight)
        etPackageContent = findViewById(R.id.etPackageContent)
        spReceiverIDType = findViewById(R.id.spReceiverIDType)
        etReceiverID = findViewById(R.id.etReceiverID)
        etReceiverName = findViewById(R.id.etReceiverName)
        etReceiverPhone = findViewById(R.id.etReceiverPhone)
        spReceiverCountryCode = findViewById(R.id.spReceiverCountryCode)
        etReceiverAddress = findViewById(R.id.etReceiverAddress)
        tilReceiverID = findViewById(R.id.tilReceiverID)
        btnReceiverGps = findViewById(R.id.btnReceiverGps)
        tvRecolectionAddress = findViewById(R.id.tvRecolectionAddress)
        spFranja = findViewById(R.id.spTimeSlot)
        etPrice = findViewById(R.id.etPrice)
        btnSend = findViewById(R.id.btnSend)
    }

    private fun setupSpinners() {
        val idTypes = listOf("CC", "TI", "CE", "Pasaporte")
        val adapterId = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, idTypes)
        spIDType.adapter = adapterId
        spReceiverIDType.adapter = adapterId
        val countryCodes = listOf("+57", "+1", "+52", "+593")
        val adapterCode =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)
        spSenderCountryCode.adapter = adapterCode
        spReceiverCountryCode.adapter = adapterCode
        val franjas = listOf("Mañana (8-12)", "Tarde (12-6)", "Noche (6-10)")
        spFranja.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, franjas)
    }

    private fun setupEndIcons() {
        tilReceiverID.setEndIconOnClickListener {
            Toast.makeText(this, "Buscar ID: ${etReceiverID.text}", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextInputLayout>(R.id.tilReceiverAddress).setEndIconOnClickListener {
            val addressText = etReceiverAddress.text.toString().trim()
            if (addressText.isNotEmpty()) {
                locationHelper.searchAddress(addressText)
            }
        }
    }

    private fun calculatePrice() {
        if (recoleccionLat == null || entregaLat == null) {
            etPrice.setText("0")
            return
        }

        val peso = etPackageWeight.text.toString().toDoubleOrNull() ?: 0.0
        val precioSimulado = 5000 + (peso * 1500)
        etPrice.setText(String.format("%.0f", precioSimulado))
    }

    private fun setupListeners() {

        val priceChangeWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                priceUpdateRunnable?.let { handler.removeCallbacks(it) }

                priceUpdateRunnable = Runnable {
                    calculatePrice()
                }

                handler.postDelayed(priceUpdateRunnable!!, 500)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etPackageHeight.addTextChangedListener(priceChangeWatcher)
        etPackageWidth.addTextChangedListener(priceChangeWatcher)
        etPackageLength.addTextChangedListener(priceChangeWatcher)
        etPackageWeight.addTextChangedListener(priceChangeWatcher)

        btnSend.setOnClickListener {
            saveSolicitud()
        }
    }

    // 🌟 MÉTODO DE GUARDADO
    private fun saveSolicitud() {

        if (!validateInputs()) return

        val clientId = sessionManager.getUserId() ?: run {
            Toast.makeText(this, "Error: Usuario no logueado.", Toast.LENGTH_LONG).show()
            return
        }

        // DTOs de Clientes y Paquete
        val remitente = ClienteRequest(
            nombre = etSenderName.text.toString().trim(),
            tipoId = spIDType.selectedItem.toString(),
            numeroId = etSenderID.text.toString().trim(),
            telefono = etSenderPhone.text.toString().trim(),
            codigoPais = spSenderCountryCode.selectedItem.toString()
        )
        val receptor = ClienteRequest(
            nombre = etReceiverName.text.toString().trim(),
            tipoId = spReceiverIDType.selectedItem.toString(),
            numeroId = etReceiverID.text.toString().trim(),
            telefono = etReceiverPhone.text.toString().trim(),
            codigoPais = spReceiverCountryCode.selectedItem.toString()
        )

        // DTO de Direcciones
        val direccionRecoleccionDto = DireccionRequest(
            direccionCompleta = recoleccionAddress!!,
            ciudad = "Bogota",
            latitud = recoleccionLat,
            longitud = recoleccionLon,
            pisoApto = null,
            notasEntrega = null
        )
        val direccionEntregaDto = DireccionRequest(
            direccionCompleta = etReceiverAddress.text.toString().trim(),
            ciudad = "Bogota",
            latitud = entregaLat,
            longitud = entregaLon,
            pisoApto = null,
            notasEntrega = null
        )

        // DTO de Paquete
        val paqueteDto = PaqueteRequest(
            peso = etPackageWeight.text.toString().toDouble(),
            alto = etPackageHeight.text.toString().toDouble(),
            ancho = etPackageWidth.text.toString().toDouble(),
            largo = etPackageLength.text.toString().toDouble(),
            contenido = etPackageContent.text.toString().trim()
        )

        // DTO SolicitudRequest (sucursalId temporalmente a 0)
        val requestDto = SolicitudRequest(
            clientId = clientId,
            remitente = remitente,
            receptor = receptor,
            direccionRecoleccion = direccionRecoleccionDto,
            direccionEntrega = direccionEntregaDto,
            paquete = paqueteDto,
            fechaRecoleccion = "2025-01-01",
            franjaHoraria = spFranja.selectedItem.toString(),
            sucursalId = 0
        )


        // 🚀 LLAMADA AL VIEWMODEL (Resuelve el Unresolved reference 'processAndSaveSolicitud')
        val recLat = recoleccionLat ?: 0.0
        val recLon = recoleccionLon ?: 0.0

        viewModel.processAndSaveSolicitud(requestDto, recLat, recLon)
    }

    private fun validateInputs(): Boolean {
        // ... (Lógica de Validación) ...
        if (recoleccionLat == null || recoleccionLon == null || recoleccionAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Falta la dirección de Recolección (vuelva a la pantalla anterior).", Toast.LENGTH_LONG).show()
            return false
        }
        if (entregaLat == null || entregaLon == null || etReceiverAddress.text.isNullOrEmpty()) {
            Toast.makeText(this, "Fije la Ubicación de Entrega usando el GPS o la búsqueda.", Toast.LENGTH_LONG).show()
            return false
        }
        if (etPackageHeight.text.toString().toDoubleOrNull() == null ||
            etPackageWidth.text.toString().toDoubleOrNull() == null ||
            etPackageLength.text.toString().toDoubleOrNull() == null ||
            etPackageWeight.text.toString().toDoubleOrNull() == null) {
            Toast.makeText(this, "Por favor llena todos los valores numéricos del paquete.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etSenderName.text.isNullOrEmpty() || etSenderID.text.isNullOrEmpty() ||
            etReceiverName.text.isNullOrEmpty() || etReceiverID.text.isNullOrEmpty()) {
            Toast.makeText(this, "Complete todos los campos de Remitente y Destinatario.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}