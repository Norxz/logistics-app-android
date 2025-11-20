package co.edu.unipiloto.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
// 🌟 IMPORTS FOR GEOCODING
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.util.Locale
// -----------------------------
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.rest.ClienteRequest
// 🌟 IMPORTS FOR API SUBMISSION
import co.edu.unipiloto.myapplication.rest.DireccionRequest
import co.edu.unipiloto.myapplication.rest.PaqueteRequest
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.rest.SolicitudRequest
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.utils.LocationHelper
import com.google.android.gms.maps.GoogleMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
// -----------------------------
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MarkerOptions

class SolicitudActivity : AppCompatActivity(), OnMapReadyCallback {

    // Remitente
    private lateinit var etSenderName: TextInputEditText
    private lateinit var etSenderID: TextInputEditText
    private lateinit var etSenderPhone: TextInputEditText
    private lateinit var spIDType: Spinner
    private lateinit var spSenderCountryCode: Spinner

    // Paquete
    private lateinit var etPackageHeight: TextInputEditText
    private lateinit var etPackageWidth: TextInputEditText
    private lateinit var etPackageLength: TextInputEditText
    private lateinit var etPackageWeight: TextInputEditText
    private lateinit var etPackageContent: TextInputEditText

    // Destinatario
    private lateinit var spReceiverIDType: Spinner
    private lateinit var etReceiverID: TextInputEditText
    private lateinit var etReceiverName: TextInputEditText
    private lateinit var etReceiverPhone: TextInputEditText
    private lateinit var etReceiverAddress: TextInputEditText
    private lateinit var spReceiverCountryCode: Spinner

    // TextInputLayouts
    private lateinit var tilReceiverID: TextInputLayout
    private lateinit var tilReceiverAddress: TextInputLayout

    // Otros
    private lateinit var spCiudad: Spinner
    private lateinit var spFranja: Spinner
    private lateinit var etPrice: TextInputEditText
    private lateinit var btnSend: View
    private lateinit var sessionManager: SessionManager // 👈 Added Session Manager

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null

    private lateinit var locationHelper: LocationHelper


    companion object {
        const val REQUEST_MAP = 1001
        const val LOCATION_PERMISSION_REQUEST = 2001
    }

    // Retained launcher for compatibility, though MapActivity launch is removed
    private val mapActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                selectedLat = data?.getDoubleExtra("lat", 0.0)
                selectedLon = data?.getDoubleExtra("lon", 0.0)
                val address = data?.getStringExtra("address") ?: ""

                etReceiverAddress.setText(address)

                Toast.makeText(this, "Ubicación seleccionada", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud)

        sessionManager = SessionManager(this)

        initViews()
        setupSpinners()
        setupEndIcons()
        setupListeners()

        // Inicializar LocationHelper
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationHelper = LocationHelper(
            this,
            fusedLocationClient,
            mapFragment
        ) { lat, lon ->
            selectedLat = lat
            selectedLon = lon
            Toast.makeText(this, "Ubicación obtenida: $lat, $lon", Toast.LENGTH_SHORT).show()
        }

        // Solicitar permisos y obtener ubicación

        // Pedir permisos y obtener ubicación
        locationHelper.checkLocationPermission(LOCATION_PERMISSION_REQUEST)
    }

    private fun initViews() {

        etSenderName = findViewById(R.id.etSenderName)
        etSenderID = findViewById(R.id.etSenderID)
        etSenderPhone = findViewById(R.id.etSenderPhone)
        spIDType = findViewById(R.id.spIDType)
        spSenderCountryCode = findViewById(R.id.spSenderCountryCode)

        // Paquete
        etPackageHeight = findViewById(R.id.etPackageHeight)
        etPackageWidth = findViewById(R.id.etPackageWidth)
        etPackageLength = findViewById(R.id.etPackageLength)
        etPackageWeight = findViewById(R.id.etPackageWeight)
        etPackageContent = findViewById(R.id.etPackageContent)

        // Destinatario
        spReceiverIDType = findViewById(R.id.spReceiverIDType)
        etReceiverID = findViewById(R.id.etReceiverID)
        etReceiverName = findViewById(R.id.etReceiverName)
        etReceiverPhone = findViewById(R.id.etReceiverPhone)
        spReceiverCountryCode = findViewById(R.id.spReceiverCountryCode)
        etReceiverAddress = findViewById(R.id.etReceiverAddress)

        // CORRECTO → ahora sí existen en tu XML
        tilReceiverID = findViewById(R.id.tilReceiverID)
        tilReceiverAddress = findViewById(R.id.tilReceiverAddress)

        // Otros
        spCiudad = findViewById(R.id.spCity)
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

        val ciudades = listOf("Zona Norte", "Zona Sur", "Zona Centro")
        spCiudad.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciudades)

        val franjas = listOf("Mañana (8-12)", "Tarde (12-6)", "Noche (6-10)")
        spFranja.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, franjas)
    }

    private fun setupEndIcons() {
        tilReceiverID.setEndIconOnClickListener {
            Toast.makeText(this, "Buscar ID: ${etReceiverID.text}", Toast.LENGTH_SHORT).show()
        }

        // Trigger Geocoding when the user clicks the address field icon
        tilReceiverAddress.setEndIconOnClickListener {
            val addressText = etReceiverAddress.text.toString().trim()
            if (addressText.isNotEmpty()) {
                geocodeAddress(addressText)
            } else {
                Toast.makeText(
                    this,
                    "Por favor ingresa una dirección primero (ej: Calle 174 #8-30).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Converts address string to LatLng
    private fun geocodeAddress(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())

        Toast.makeText(this, "Buscando dirección: $address...", Toast.LENGTH_SHORT).show()

        // Run the geocoding operation on a background thread
        Thread {
            try {
                val addresses = geocoder.getFromLocationName(address, 1)

                Handler(Looper.getMainLooper()).post {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val firstAddress = addresses[0]

                        // Update coordinates and map
                        selectedLat = firstAddress.latitude
                        selectedLon = firstAddress.longitude

                        val canonicalAddress = firstAddress.getAddressLine(0) ?: address
                        etReceiverAddress.setText(canonicalAddress)

                        Toast.makeText(
                            this,
                            "Dirección localizada. Coordenadas fijadas.",
                            Toast.LENGTH_SHORT
                        ).show()

                        locationHelper.updateMapLocation()
                    } else {
                        Toast.makeText(
                            this,
                            "Dirección no encontrada. Intenta otra dirección.",
                            Toast.LENGTH_LONG
                        ).show()
                        selectedLat = null
                        selectedLon = null
                    }
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this,
                        "Error de red/servicio de geocodificación.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("SolicitudActivity", "Geocoding failed: ${e.message}")
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this,
                        "Ocurrió un error inesperado al buscar la dirección.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("SolicitudActivity", "Unexpected error during geocoding: ${e.message}")
            }
        }.start()
    }


    private fun setupListeners() {

        btnSend.setOnClickListener {

            val senderName = etSenderName.text.toString().trim()
            val senderId = etSenderID.text.toString().trim()
            val senderPhone = etSenderPhone.text.toString().trim()

            val height = etPackageHeight.text.toString().toDoubleOrNull()
            val width = etPackageWidth.text.toString().toDoubleOrNull()
            val length = etPackageLength.text.toString().toDoubleOrNull()
            val weight = etPackageWeight.text.toString().toDoubleOrNull()
            val price = etPrice.text.toString().toDoubleOrNull()

            val receiverId = etReceiverID.text.toString().trim()
            val receiverPhone = etReceiverPhone.text.toString().trim()
            val receiverAddress = etReceiverAddress.text.toString().trim()
            val receiverName = etReceiverName.text.toString()

            val city = spCiudad.selectedItem.toString()
            val timeSlot = spFranja.selectedItem.toString()
            val fechaRecoleccion = "2025-01-01"

            val zona = when (city) {
                "Zona Norte" -> "ZN"
                "Zona Sur" -> "ZS"
                "Zona Centro" -> "ZC"
                else -> "ND"
            }

            // 1. VALIDATION
            if (height == null || width == null || length == null || weight == null || price == null) {
                Toast.makeText(
                    this,
                    "Por favor llena todos los valores numéricos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (selectedLat == null || selectedLon == null) {
                Toast.makeText(
                    this,
                    "Por favor busca una dirección válida para fijar la ubicación.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Get logged-in client ID (Crucial for API submission)
            val clientId = sessionManager.getUserId() ?: run {
                Toast.makeText(
                    this,
                    "Error: Usuario no logueado. Cierre sesión y vuelva a iniciar.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

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


            val requestDto = SolicitudRequest(
                clientId = clientId,
                remitente = remitente,
                receptor = receptor,
                direccion = DireccionRequest(
                    direccionCompleta = etReceiverAddress.text.toString().trim(),
                    ciudad = spCiudad.selectedItem.toString(),
                    latitud = selectedLat,
                    longitud = selectedLon,
                    pisoApto = null,
                    notasEntrega = null
                ),
                paquete = PaqueteRequest(
                    peso = etPackageWeight.text.toString().toDouble(),
                    alto = etPackageHeight.text.toString().toDouble(),
                    ancho = etPackageWidth.text.toString().toDouble(),
                    largo = etPackageLength.text.toString().toDouble(),
                    contenido = etPackageContent.text.toString().trim()
                ),
                fechaRecoleccion = fechaRecoleccion,
                franjaHoraria = spFranja.selectedItem.toString()
            )


            // 3. 🚀 API CALL
            RetrofitClient.apiService.crearSolicitud(requestDto)
                .enqueue(object : Callback<Solicitud> {
                    override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@SolicitudActivity,
                                "¡Solicitud enviada (Guía #${response.body()?.id})!",
                                Toast.LENGTH_LONG
                            ).show()

                            val usuarioEmail = sessionManager.getUserEmail()
                            if (usuarioEmail.isNullOrEmpty()) {
                                Toast.makeText(
                                    this@SolicitudActivity,
                                    "No hay email en sesión. Por favor inicia sesión primero.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            }

                            val intent = Intent(
                                this@SolicitudActivity,
                                GuideConfirmationActivity::class.java
                            )
                            intent.putExtra("solicitudId", response.body()?.id)
                            intent.putExtra("usuarioEmail", usuarioEmail)
                            startActivity(intent)


                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("API_CALL", "Error ${response.code()}: $errorBody")
                            Toast.makeText(
                                this@SolicitudActivity,
                                "Error ${response.code()} al enviar: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                        Log.e("API_CALL", "Fallo de red: ${t.message}")
                        Toast.makeText(
                            this@SolicitudActivity,
                            "Fallo de conexión. Verifique el servidor.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })

            // Original Log.d removed, as API call is now primary action
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    fun geocodeAddress(
        address: String,
        onResult: (lat: Double, lon: Double, formattedAddress: String) -> Unit
    ) {
        Thread {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val firstAddress = addresses[0]
                    val lat = firstAddress.latitude
                    val lon = firstAddress.longitude
                    val formatted = firstAddress.getAddressLine(0) ?: address
                    Handler(Looper.getMainLooper()).post {
                        onResult(lat, lon, formatted)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Error al geocodificar", Toast.LENGTH_LONG).show()
                    Log.e("SolicitudActivity", e.message ?: "Geocode error")
                }
            }
        }.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (selectedLat != null && selectedLon != null) {
            val initialLatLng = LatLng(selectedLat!!, selectedLon!!)
            googleMap.addMarker(
                MarkerOptions()
                    .position(initialLatLng)
                    .title("Destino")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f))
        } else {
            Toast.makeText(this, "Ubicación no definida", Toast.LENGTH_SHORT).show()
        }
    }

}