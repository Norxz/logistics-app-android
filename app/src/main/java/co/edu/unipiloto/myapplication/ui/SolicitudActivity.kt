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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.ClienteRequest
import co.edu.unipiloto.myapplication.dto.DireccionRequest
import co.edu.unipiloto.myapplication.dto.PaqueteRequest
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudRequest
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.utils.LocationHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.maps.MapView

class SolicitudActivity : AppCompatActivity() {

    // ... (Variables de Vistas - No requieren cambios)
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
    // -------------------------------------------------------------------

    // 🌟 Nueva instancia de Handler para Debounce
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
            locationHelper.getCurrentLocation { address, lat, lon ->
                etReceiverAddress.setText(address)
                entregaLat = lat
                entregaLon = lon
                calculatePrice() // Llamada al cálculo de precio al usar GPS
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

        // 🌟 LLAMADA A LISTENERS DESPUÉS DE LA CONFIGURACIÓN BÁSICA
        setupListeners()
    }

    // ... (Métodos de ciclo de vida para MapView - No requieren cambios)
    override fun onResume() { super.onResume(); findViewById<MapView>(R.id.mapViewReceiver).onResume() }
    override fun onPause() { super.onPause(); findViewById<MapView>(R.id.mapViewReceiver).onPause() }
    override fun onDestroy() { super.onDestroy(); findViewById<MapView>(R.id.mapViewReceiver).onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); findViewById<MapView>(R.id.mapViewReceiver).onLowMemory() }
    // ---------------------------------------------


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
        // ... (Asignación de Vistas - No requieren cambios)
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
        // ... (Configuración de Spinners - No requieren cambios)
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
        // Validación básica para evitar errores antes de calcular
        if (recoleccionLat == null || entregaLat == null) {
            etPrice.setText("0")
            return
        }

        val peso = etPackageWeight.text.toString().toDoubleOrNull() ?: 0.0
        val precioSimulado = 5000 + (peso * 1500)
        etPrice.setText(String.format("%.0f", precioSimulado))
        // TODO: En un caso real, realizar la llamada Retrofit para obtener el precio real
    }

    private fun setupListeners() {

        // 🌟 CORRECCIÓN: Usar una clase anónima TextWatcher
        val priceChangeWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Limpiar cualquier ejecución anterior
                priceUpdateRunnable?.let { handler.removeCallbacks(it) }

                // Crear un nuevo Runnable
                priceUpdateRunnable = Runnable {
                    calculatePrice()
                }

                // Programar el cálculo 500ms después de la última pulsación (Debounce)
                handler.postDelayed(priceUpdateRunnable!!, 500)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        // 🌟 Asignación del TextWatcher (ahora es del tipo correcto)
        etPackageHeight.addTextChangedListener(priceChangeWatcher)
        etPackageWidth.addTextChangedListener(priceChangeWatcher)
        etPackageLength.addTextChangedListener(priceChangeWatcher)
        etPackageWeight.addTextChangedListener(priceChangeWatcher)
        // -------------------------------------------------------------------


        btnSend.setOnClickListener {

            if (!validateInputs()) return@setOnClickListener

            val clientId = sessionManager.getUserId() ?: run {
                Toast.makeText(this, "Error: Usuario no logueado.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // DTOs de Clientes
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
                ciudad = "Bogota", // Esto debería ser dinámico
                latitud = recoleccionLat,
                longitud = recoleccionLon,
                pisoApto = null,
                notasEntrega = null
            )
            val direccionEntregaDto = DireccionRequest(
                direccionCompleta = etReceiverAddress.text.toString().trim(),
                ciudad = "Bogota", // Esto debería ser dinámico
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

            // DTO FINAL SolicitudRequest
            val requestDto = SolicitudRequest(
                clientId = clientId,
                remitente = remitente,
                receptor = receptor,
                direccionRecoleccion = direccionRecoleccionDto,
                direccionEntrega = direccionEntregaDto,
                paquete = paqueteDto,
                fechaRecoleccion = "2025-01-01",
                franjaHoraria = spFranja.selectedItem.toString(),
                sucursalId = 1 // Hardcodeado
            )


            // 3. 🚀 API CALL
            // 🌟 CORRECCIÓN: Acceder a la interfaz de servicio REST
            lifecycleScope.launch { // Inicia un Coroutine Scope
                try {
                    // Ejecuta la función suspend en un hilo de I/O
                    val response = withContext(Dispatchers.IO) {
                        // Llama directamente a la función suspend, que devuelve Response<T>
                        RetrofitClient.solicitudService.crearSolicitud(requestDto)
                    }

                    // Manejo de la respuesta (de vuelta en el Main Thread)
                    if (response.isSuccessful) {
                        val solicitudId = response.body()?.id

                        Toast.makeText(
                            this@SolicitudActivity,
                            "¡Solicitud enviada (Guía #$solicitudId)!",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@SolicitudActivity, GuideConfirmationActivity::class.java)
                        intent.putExtra("solicitudId", solicitudId)
                        intent.putExtra("usuarioEmail", sessionManager.getUserEmail())
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_CALL", "Error ${response.code()}: $errorBody")
                        Toast.makeText(
                            this@SolicitudActivity,
                            "Error ${response.code()} al enviar: ${response.message()}. Revise logs.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (t: Throwable) {
                    // Captura errores de red/conexión
                    Log.e("API_CALL", "Fallo de red: ${t.message}")
                    Toast.makeText(
                        this@SolicitudActivity,
                        "Fallo de conexión. Verifique el servidor y la red.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        // ... (Lógica de Validación - No requiere cambios)
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