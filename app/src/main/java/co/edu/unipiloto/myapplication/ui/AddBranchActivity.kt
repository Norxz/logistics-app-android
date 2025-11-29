package co.edu.unipiloto.myapplication.ui

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.DireccionRequest
import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.dto.RetrofitClient // Suponemos que existe
import co.edu.unipiloto.myapplication.repository.SucursalRepository // Suponemos que existe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

/**
 * Activity para el registro y edición de sucursales.
 */
class AddBranchActivity : AppCompatActivity(), OnMapReadyCallback {

    // --- Vistas ---
    private lateinit var tvHeaderTitle: TextView
    private lateinit var etBranchName: TextInputEditText
    private lateinit var etAddressComplete: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etAptoPiso: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSaveBranch: MaterialButton
    private lateinit var btnBranchGps: ImageButton
    private lateinit var mapViewBranch: MapView

    // --- Mapa, Ubicación y ViewModel ---
    private lateinit var googleMap: GoogleMap
    private var isEditMode: Boolean = false
    private var branchId: Int? = null
    private lateinit var viewModel: AddBranchViewModel // ⬅️ DECLARACIÓN DEL VIEWMODEL

    // ------------------------------------
    // --- LIFECYCLE & SETUP ---
    // ------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_branch)

        // Inicializar vistas
        initViews()

        // Inicializar el MapView
        mapViewBranch.onCreate(savedInstanceState)
        mapViewBranch.getMapAsync(this)

        // Comprobar modo edición
        branchId = intent.getIntExtra("BRANCH_ID", -1).takeIf { it != -1 }
        isEditMode = branchId != null

        setupUI(isEditMode)
        setupViewModel() // ⬅️ INICIALIZA EL VIEWMODEL
        setupListeners()
        setupObservers() // ⬅️ CONFIGURA LOS OBSERVADORES DE RESPUESTA
        // if (isEditMode) loadBranchData()
    }

    private fun initViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        etBranchName = findViewById(R.id.etBranchName)
        etAddressComplete = findViewById(R.id.etAddressComplete)
        etCity = findViewById(R.id.etCity)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etAptoPiso = findViewById(R.id.etAptoPiso)
        etNotes = findViewById(R.id.etNotes)
        btnSaveBranch = findViewById(R.id.btnSaveBranch)
        btnBranchGps = findViewById(R.id.btnBranchGps)
        mapViewBranch = findViewById(R.id.mapViewBranch)
    }

    private fun setupUI(isEdit: Boolean) {
        if (isEdit) {
            tvHeaderTitle.text = "Editar Sucursal (ID: $branchId)"
            btnSaveBranch.text = "ACTUALIZAR SUCURSAL"
        } else {
            tvHeaderTitle.text = "Registrar Nueva Sucursal"
            btnSaveBranch.text = "GUARDAR SUCURSAL"
        }
    }

    // ------------------------------------
    // --- VIEWMODEL & OBSERVERS ---
    // ------------------------------------

    private fun setupViewModel() {
        // Asumimos que RetrofitClient.sucursalService y SucursalRepository existen
        val repository = SucursalRepository(RetrofitClient.sucursalService)
        val factory = AddBranchViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AddBranchViewModel::class.java]
    }

    private fun setupObservers() {
        // Observa el LiveData que notifica el resultado de guardado/actualización
        viewModel.saveResult.observe(this) { result ->
            result.onSuccess {
                val message = if (isEditMode) "Sucursal actualizada con éxito." else "Sucursal registrada con éxito."
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("AddBranchActivity", "Error de guardado: ${exception.message}")
            }
        }
    }

    // ------------------------------------
    // --- LISTENERS & DATA LOGIC ---
    // ------------------------------------

    private fun setupListeners() {
        btnSaveBranch.setOnClickListener { saveOrUpdateBranch() }
        btnBranchGps.setOnClickListener { findCurrentLocation() }

        val tilAddressComplete = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilAddressComplete)
        tilAddressComplete.setEndIconOnClickListener {
            geocodeAddress(etAddressComplete.text.toString())
        }
    }

    /**
     * Maneja el proceso de guardar o actualizar la sucursal.
     */
    private fun saveOrUpdateBranch() {
        val sucursalRequest = buildSucursalRequest()

        if (sucursalRequest == null) {
            return
        }

        // 5. Llamada al ViewModel (Referencias resueltas)
        if (isEditMode && branchId != null) {
            viewModel.updateBranch(branchId!!, sucursalRequest)
        } else {
            viewModel.saveBranch(sucursalRequest)
        }
    }

    // ... (El resto de funciones de geocodificación y validación se mantienen) ...
    // Para simplificar, solo incluiré las funciones que habías proporcionado/aceptado previamente.

    // ... (Funciones de Soporte) ...

    private fun buildSucursalRequest(): SucursalRequest? {
        if (!validateInput()) return null

        val nombre = etBranchName.text.toString().trim()
        val latitud = etLatitude.text.toString().trim().toDouble()
        val longitud = etLongitude.text.toString().trim().toDouble()
        val direccionCompleta = etAddressComplete.text.toString().trim()
        val ciudad = etCity.text.toString().trim()
        val pisoApto = etAptoPiso.text.toString().trim()
        val notas = etNotes.text.toString().trim()

        val direccionRequest = DireccionRequest(
            direccionCompleta = direccionCompleta,
            ciudad = ciudad,
            latitud = latitud,
            longitud = longitud,
            pisoApto = if (pisoApto.isBlank()) null else pisoApto,
            notasEntrega = if (notas.isBlank()) null else notas,
            barrio = null,
            codigoPostal = null,
            tipoDireccion = "SUCURSAL"
        )

        return SucursalRequest(
            nombre = nombre,
            direccion = direccionRequest
        )
    }

    private fun validateInput(): Boolean {
        val nombre = etBranchName.text.toString().trim()
        val latitudStr = etLatitude.text.toString().trim()
        val longitudStr = etLongitude.text.toString().trim()
        val direccionCompleta = etAddressComplete.text.toString().trim()
        val ciudad = etCity.text.toString().trim()

        if (nombre.isBlank() || direccionCompleta.isBlank() || latitudStr.isBlank() || longitudStr.isBlank() || ciudad.isBlank()) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios y defina la ubicación.", Toast.LENGTH_LONG).show()
            return false
        }

        val latitud = latitudStr.toDoubleOrNull()
        val longitud = longitudStr.toDoubleOrNull()

        if (latitud == null || longitud == null) {
            Toast.makeText(this, "Las coordenadas no son válidas.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun findCurrentLocation() {
        Toast.makeText(this, "Implementar lógica de GPS y permisos.", Toast.LENGTH_SHORT).show()
    }

    private fun updateMapAndCoordinates(latLng: LatLng, city: String?) {
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title("Ubicación de Sucursal"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        etLatitude.setText(latLng.latitude.toString())
        etLongitude.setText(latLng.longitude.toString())
        if (!city.isNullOrBlank()) {
            etCity.setText(city)
        }
    }

    private fun geocodeAddress(address: String) {
        if (address.isBlank()) {
            Toast.makeText(this, "Ingrese una dirección válida.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val latLng = performGeocoding(address)

            if (latLng != null) {
                updateMapAndCoordinates(latLng, null)
                Toast.makeText(this@AddBranchActivity, "Dirección encontrada.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddBranchActivity, "Dirección no encontrada.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun performGeocoding(address: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(this@AddBranchActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                return@withContext LatLng(location.latitude, location.longitude)
            }
            return@withContext null
        } catch (e: IOException) {
            Log.e("AddBranchActivity", "Error de Geocoding: ${e.message}")
            return@withContext null
        }
    }

    private fun reverseGeocodeLocation(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0)
                val city = address.locality

                etAddressComplete.setText(fullAddress)
                updateMapAndCoordinates(latLng, city)
            } else {
                updateMapAndCoordinates(latLng, null)
                Toast.makeText(this, "Ubicación seleccionada, dirección no encontrada.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("AddBranchActivity", "Error de Reverse Geocoding: ${e.message}")
            updateMapAndCoordinates(latLng, null)
        }
    }


    // ------------------------------------
    // --- MAPS LIFECYCLE ---
    // ------------------------------------

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        val defaultLocation = LatLng(4.6534, -74.0836) // Bogotá, Colombia
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        googleMap.setOnMapClickListener { latLng ->
            reverseGeocodeLocation(latLng)
        }
    }

    override fun onResume() {
        super.onResume()
        mapViewBranch.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapViewBranch.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapViewBranch.onStop()
    }

    override fun onPause() {
        mapViewBranch.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapViewBranch.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewBranch.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewBranch.onSaveInstanceState(outState)
    }
}