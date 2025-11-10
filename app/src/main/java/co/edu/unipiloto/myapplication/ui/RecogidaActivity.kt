package co.edu.unipiloto.myapplication.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import co.edu.unipiloto.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


/**
 * Activity dedicada a la selección de una dirección (Recolección o Entrega).
 * IMPLEMENTA: Lógica de Mapa con OSMDroid.
 */
class RecogidaActivity : AppCompatActivity() { // Ya no implementa OnMapReadyCallback

    // --- CONSTANTES Y GEOLOCALIZACIÓN ---
    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        const val EXTRA_IS_RECOLECTION = "EXTRA_IS_RECOLECTION"
        private const val DEFAULT_ZOOM = 15.0
        private val DEFAULT_LOCATION = GeoPoint(4.7110, -74.0721) // Bogotá, como referencia
    }

    // Propiedades de Mapa y Geolocalización
    private lateinit var mapView: MapView // 🟢 Vista de mapa OSMDroid
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: GeoPoint? = null // 🟢 Usaremos GeoPoint

    // Lanzador de permisos (el mismo)
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getDeviceLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
        }
    }


    // --- VISTAS ---
    private lateinit var etAddress: TextInputEditText
    private lateinit var tilAddress: TextInputLayout
    private lateinit var btnGoBack: ImageButton
    private lateinit var btnUseGps: ImageButton
    private lateinit var btnContinue: MaterialButton
    private var isRecolectionAddress = false

    // ---------------------------------------------------------------------
    // Lógica del Ciclo de Vida
    // ---------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🟢 Carga la configuración de OSMDroid (CRÍTICO para que el mapa funcione)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        // Usamos el layout del XML que tiene el MapView
        setContentView(R.layout.activity_recogida)

        supportActionBar?.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        isRecolectionAddress = intent.getBooleanExtra(EXTRA_IS_RECOLECTION, false)

        initViews()
        setupMap() // Llama a la configuración de OSMDroid
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // CRÍTICO para OSMDroid
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // CRÍTICO para OSMDroid
    }

    // ---------------------------------------------------------------------
    // Lógica de Inicialización
    // ---------------------------------------------------------------------

    private fun initViews() {
        btnGoBack = findViewById(R.id.btnGoBack)
        etAddress = findViewById(R.id.etAddress)
        tilAddress = findViewById(R.id.tilAddress)
        btnUseGps = findViewById(R.id.btnUseGps)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun setupMap() {
        mapView = findViewById(R.id.mapPlaceholder) // Mapear la vista MapView
        mapView.setTileSource(TileSourceFactory.MAPNIK) // Proveedor de mapas
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(DEFAULT_ZOOM)
        mapController.setCenter(DEFAULT_LOCATION)

        updateMarker(DEFAULT_LOCATION, "Punto de Interés")

        mapView.setOnLongClickListener {
            // Obtener las coordenadas del centro de la pantalla
            val centerPoint = mapView.projection.fromPixels(mapView.width / 2, mapView.height / 2)

            reverseGeocode(centerPoint as GeoPoint) // Determina la dirección
            updateMarker(centerPoint as GeoPoint, "Ubicación Seleccionada") // Mueve el marcador

            true // Indica que el evento fue consumido
        }
    }

    private fun setupListeners() {
        btnGoBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnUseGps.setOnClickListener {
            checkLocationPermission() // Inicia el flujo de GPS
        }

        btnContinue.setOnClickListener {
            navigateToSolicitud()
        }

        etAddress.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && etAddress.text?.isNotEmpty() == true) {
                geocodeAddress(etAddress.text.toString())
            }
        }
    }

    // ---------------------------------------------------------------------
    // Lógica de Geolocalización y Mapas (OSMDroid)
    // ---------------------------------------------------------------------

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation()
        } else {
            locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

            fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val currentGeoPoint = GeoPoint(location.latitude, location.longitude)

                    reverseGeocode(currentGeoPoint)
                    updateMarker(currentGeoPoint, "Mi Ubicación Actual") // Mueve el mapa y marca

                } else {
                    Toast.makeText(this, "No se encontró ubicación. Usando ubicación por defecto.", Toast.LENGTH_SHORT).show()
                    updateMarker(DEFAULT_LOCATION, "Bogotá (Defecto)")
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de seguridad de ubicación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Mueve el mapa y actualiza el marcador central.
     */
    private fun updateMarker(geoPoint: GeoPoint, title: String) {
        mapView.overlays.clear()

        val marker = Marker(mapView).apply {
            position = geoPoint
            this.title = title
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)
        mapView.invalidate()
        lastKnownLocation = geoPoint // Guardar la ubicación final
    }

    /**
     * Convierte coordenadas (GeoPoint) a una dirección legible (String) y actualiza etAddress.
     */
    private fun reverseGeocode(geoPoint: GeoPoint) {
        lastKnownLocation = geoPoint
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            // El Geocoder nativo de Android aún funciona con las coordenadas
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                etAddress.setText(address)
                etAddress.setSelection(address.length)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error de servicio de Geocodificación Inversa.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Convierte una dirección (String) a coordenadas (GeoPoint) y mueve el mapa.
     */
    private fun geocodeAddress(address: String) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            if (!addresses.isNullOrEmpty()) {
                val p = addresses[0]
                val newGeoPoint = GeoPoint(p.latitude, p.longitude)
                updateMarker(newGeoPoint, address) // Mueve y marca
            } else {
                Toast.makeText(this, "Dirección no encontrada.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error de búsqueda de dirección.", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------------------
    // Devolver Resultado
    // ---------------------------------------------------------------------

    private fun navigateToSolicitud() {
        val address = etAddress.text.toString().trim()
        val finalGeoPoint = lastKnownLocation ?: DEFAULT_LOCATION

        if (address.isEmpty() || address.length < 5) {
            tilAddress.error = "Por favor, ingrese una dirección válida."
            return
        }
        tilAddress.error = null // Limpiar error si todo está bien

        // 1. Crear el Intent para ir a SolicitudActivity
        val intent = Intent(this, SolicitudActivity::class.java).apply {
            // 2. Adjuntar los datos de la dirección de Recolección como extras
            putExtra("RECOLECTION_ADDRESS", address)
            putExtra("RECOLECTION_LATITUDE", finalGeoPoint.latitude)
            putExtra("RECOLECTION_LONGITUDE", finalGeoPoint.longitude)

            // Puedes añadir más lógica aquí si RecogidaActivity se usa para múltiples propósitos
        }

        // 3. Iniciar la actividad
        startActivity(intent)

        finish()
    }
}