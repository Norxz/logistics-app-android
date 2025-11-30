package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.DireccionRequest
import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.repository.SucursalRepository
import co.edu.unipiloto.myapplication.utils.LocationHelper // ⬅️ Mantener este import
import com.google.android.gms.maps.MapView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

// ⬅️ Ya no implementamos OnMapReadyCallback aquí.
class AddBranchActivity : AppCompatActivity() {

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
    // private lateinit var googleMap: GoogleMap // ⬅️ ELIMINADO
    private var isEditMode: Boolean = false
    private var branchId: Int? = null
    private lateinit var viewModel: AddBranchViewModel

    // ⬅️ INSTANCIA DE LOCATION HELPER
    private lateinit var locationHelper: LocationHelper

    // ------------------------------------
    // --- LIFECYCLE & SETUP ---
    // ------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_branch)

        initViews()

        // Inicializar el MapView
        mapViewBranch.onCreate(savedInstanceState)

        branchId = intent.getIntExtra("BRANCH_ID", -1).takeIf { it != -1 }
        isEditMode = branchId != null

        // ⬅️ INICIALIZACIÓN DE LOCATION HELPER
        locationHelper = LocationHelper(activity = this,
            mapView = mapViewBranch,
            // ⬅️ El callback ahora acepta la CIUDAD
            onLocationSelected = { address, lat, lon, city ->
                // Este callback se llama cuando el usuario selecciona o busca una ubicación
                etAddressComplete.setText(address)
                etLatitude.setText(lat.toString())
                etLongitude.setText(lon.toString())

                // ⬅️ AHORA LLENAMOS EL CAMPO etCity
                if (!city.isNullOrBlank()) {
                    etCity.setText(city)
                } else {
                    // Opcional: limpiar o dejar un valor predeterminado si no se encuentra
                    etCity.setText("")
                }

                Toast.makeText(this, "Ubicación actualizada.", Toast.LENGTH_SHORT).show()
            })

        setupUI(isEditMode)
        setupViewModel()
        setupListeners()
        setupObservers()
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
        val repository = SucursalRepository(RetrofitClient.sucursalService)
        val factory = AddBranchViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AddBranchViewModel::class.java]
    }

    private fun setupObservers() {
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

        // ⬅️ DELEGA al LocationHelper para obtener GPS
        btnBranchGps.setOnClickListener {
            locationHelper.getCurrentLocation { address, lat, lon ->
                // El callback ya está definido en la inicialización del helper
            }
        }

        val tilAddressComplete = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilAddressComplete)
        tilAddressComplete.setEndIconOnClickListener {
            val address = etAddressComplete.text.toString()
            if (address.isBlank()) {
                Toast.makeText(this, "Ingrese una dirección válida.", Toast.LENGTH_SHORT).show()
                return@setEndIconOnClickListener
            }
            // ⬅️ DELEGA al LocationHelper para buscar dirección
            locationHelper.searchAddress(address)
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

        if (isEditMode && branchId != null) {
            viewModel.updateBranch(branchId!!, sucursalRequest)
        } else {
            viewModel.saveBranch(sucursalRequest)
        }
    }

    // --- Funciones de Soporte (Se mantienen, ya que son de DTO/Validación) ---

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