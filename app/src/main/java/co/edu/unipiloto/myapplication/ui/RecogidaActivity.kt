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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.MapsInitializer


/**
 * Activity dedicada a la selección de una dirección (Recolección o Entrega).
 * IMPLEMENTA: Lógica de Mapa con OSMDroid.
 */
class RecogidaActivity : AppCompatActivity(), OnMapReadyCallback { // Ya no implementa OnMapReadyCallback

    // --- CONSTANTES Y GEOLOCALIZACIÓN ---
    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        const val EXTRA_IS_RECOLECTION = "EXTRA_IS_RECOLECTION"
        private const val DEFAULT_ZOOM = 15.0f
        private val DEFAULT_LOCATION = LatLng(4.7110, -74.0721) // Bogotá, como referencia
    }

    // Propiedades de Mapa y Geolocalización
    private lateinit var mapView: MapView // 🟢 Vista de mapa OSMDroid

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: LatLng? = null // 🟢 Usaremos GeoPoint

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

        // Usamos el layout del XML que tiene el MapView
        setContentView(R.layout.activity_recogida)

        supportActionBar?.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        isRecolectionAddress = intent.getBooleanExtra(EXTRA_IS_RECOLECTION, false)

        initViews()
        setupMap(savedInstanceState) // Llama a la configuración de OSMDroid
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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

    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.mapPlaceholder)
        MapsInitializer.initialize(applicationContext) // Inicializar Google Maps
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // Llamar a onMapReady cuando el mapa esté listo
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true

        // Mover la cámara a la ubicación por defecto
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))

        updateMarker(DEFAULT_LOCATION, "Punto de Interés")

        // 🟢 Manejar Long Click (Clic largo) para seleccionar ubicación
        googleMap.setOnMapLongClickListener { latLng ->
            reverseGeocode(latLng) // Determina la dirección
            updateMarker(latLng, "Ubicación Seleccionada") // Mueve el marcador
        }

        // 🟢 Manejar el clic en el mapa (opcional, si quieres que al tocar se mueva el marcador)
        googleMap.setOnMapClickListener { latLng ->
            reverseGeocode(latLng)
            updateMarker(latLng, "Ubicación Seleccionada")
        }
    }


    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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

                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    reverseGeocode(currentLatLng) // 👈 Usar LatLng
                    updateMarker(currentLatLng, "Mi Ubicación Actual") // 👈 Usar LatLng

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
    private fun updateMarker(latLng: LatLng, title: String) {
        googleMap.clear() // Limpia todos los marcadores

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)

        googleMap.addMarker(markerOptions)

        // 🟢 Mueve la cámara
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))

        lastKnownLocation = latLng // Guardar la ubicación final
    }

    /**
     * Convierte coordenadas (GeoPoint) a una dirección legible (String) y actualiza etAddress.
     */
    private fun reverseGeocode(latLng: LatLng) {
        lastKnownLocation = latLng
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            // El Geocoder nativo de Android funciona con LatLng
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

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
                // 🟢 Usar LatLng
                val newLatLng = LatLng(p.latitude, p.longitude)
                updateMarker(newLatLng, address) // Mueve y marca
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
        val finalLatLng = lastKnownLocation ?: DEFAULT_LOCATION // 🟢 Usar LatLng

        if (address.isEmpty() || address.length < 5) {
            tilAddress.error = "Por favor, ingrese una dirección válida."
            return
        }
        tilAddress.error = null

        val intent = Intent(this, SolicitudActivity::class.java).apply {
            putExtra("RECOLECTION_ADDRESS", address)
            // 🟢 Usar LatLng
            putExtra("RECOLECTION_LATITUDE", finalLatLng.latitude)
            putExtra("RECOLECTION_LONGITUDE", finalLatLng.longitude)
        }

        startActivity(intent)
        finish()
    }
}