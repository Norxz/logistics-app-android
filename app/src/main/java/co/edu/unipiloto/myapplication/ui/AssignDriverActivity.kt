package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// --- IMPORTS RESTAURADOS PARA COROUTINES ---
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
// ------------------------------------------
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.api.UserApi
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.dto.UserResponse
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.ui.adapter.AssignedRequestAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Actividad para la gestión de asignación de conductores y monitoreo del estado de las solicitudes.
 * Rol: Gestor/Administrador.
 */
class AssignDriverActivity : AppCompatActivity() {

    // Vistas del Layout
    private lateinit var etSolicitudId: EditText
    private lateinit var tvSolicitudDetails: TextView
    private lateinit var spinnerDrivers: Spinner
    private lateinit var btnAssignDriver: Button
    private lateinit var rvAssignedRequests: RecyclerView
    private lateinit var tvNoRequests: TextView

    // Variables de Lógica
    private lateinit var requestAdapter: AssignedRequestAdapter
    private var selectedDriverId: Long? = null
    private var selectedSolicitudId: Long? = null

    // 🏆 VARIABLES PARA MANEJO DINÁMICO
    private lateinit var driverNamesList: List<String>
    private var driverIdToNameMap: Map<String, Long> = emptyMap() // DisplayName -> ID

    // Servicios y Gestores
    private lateinit var solicitudApi: SolicitudApi
    private lateinit var userApi: UserApi // ⬅️ AGREGADO
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_driver)

        // --- INICIALIZACIÓN DE SERVICIOS ---
        solicitudApi = RetrofitClient.getSolicitudApi()
        userApi = RetrofitClient.getUserApi() // ⬅️ Inicializar UserApi
        sessionManager = SessionManager(this)

        supportActionBar?.title = "Asignación y Monitoreo Logístico"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupDriverSpinner()
        setupRecyclerView()
        setupListeners()
        loadRequestsForMonitoring()
    }

    private fun initViews() {
        etSolicitudId = findViewById(R.id.etSolicitudId)
        tvSolicitudDetails = findViewById(R.id.tvSolicitudDetails)
        spinnerDrivers = findViewById(R.id.spinnerDrivers)
        btnAssignDriver = findViewById(R.id.btnAssignDriver)
        rvAssignedRequests = findViewById(R.id.rvAssignedRequests)
        tvNoRequests = findViewById(R.id.tvNoRequests)
    }

    // -------------------------------------------------------------------------
    // --- 1. CONFIGURACIÓN DEL SPINNER DE CONDUCTORES (USANDO COROUTINES) ---
    // -------------------------------------------------------------------------
    private fun setupDriverSpinner() {
        val sucursalId = sessionManager.getSucursalId()

        // Convertir Any a Long? de forma segura
        val sucursalIdLong = (sucursalId as? String)?.toLongOrNull() ?: (sucursalId as? Long)

        if (sucursalIdLong == null || sucursalIdLong <= 0) {
            Toast.makeText(this, "Error: Sucursal no válida. No se pueden cargar conductores.", Toast.LENGTH_LONG).show()
            driverNamesList = listOf("Error de sucursal")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, driverNamesList)
            spinnerDrivers.adapter = adapter
            return
        }

        // 🏆 CORRECCIÓN: Usar lifecycleScope.launch para llamar a la función suspendida
        lifecycleScope.launch {
            try {
                // 1. Llamada a la API de usuarios (conductores)
                val response = userApi.getConductoresBySucursal(sucursalIdLong)

                if (response.isSuccessful) {
                    val drivers = response.body() ?: emptyList()

                    // 2. Procesar los datos
                    val names = mutableListOf("Seleccionar Conductor")
                    val idMap = mutableMapOf<String, Long>()

                    // Elemento inicial
                    idMap["Seleccionar Conductor"] = 0L

                    drivers.forEach { driver ->
                        // Asumo que UserResponse tiene id y fullName
                        val displayName = "${driver.fullName} (ID: ${driver.id})"
                        names.add(displayName)
                        idMap[displayName] = driver.id
                    }

                    driverNamesList = names
                    driverIdToNameMap = idMap

                    // 3. Poblar el Spinner
                    val adapter = ArrayAdapter(
                        this@AssignDriverActivity,
                        android.R.layout.simple_spinner_item,
                        driverNamesList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDrivers.adapter = adapter

                } else {
                    Toast.makeText(this@AssignDriverActivity, "Error ${response.code()} al cargar conductores.", Toast.LENGTH_SHORT).show()
                    driverNamesList = listOf("Error de API")
                    spinnerDrivers.adapter = ArrayAdapter(this@AssignDriverActivity, android.R.layout.simple_spinner_item, driverNamesList)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssignDriverActivity, "Error de red al cargar conductores: ${e.message}", Toast.LENGTH_SHORT).show()
                driverNamesList = listOf("Fallo de conexión")
                spinnerDrivers.adapter = ArrayAdapter(this@AssignDriverActivity, android.R.layout.simple_spinner_item, driverNamesList)
            }
        }

        // 4. Configurar el Listener del Spinner
        spinnerDrivers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDisplayName = parent.getItemAtPosition(position).toString()

                val selectedId = driverIdToNameMap[selectedDisplayName]

                // Si el ID es 0L (Seleccionar Conductor) o nulo, se establece a null.
                selectedDriverId = if (selectedId != null && selectedId > 0) selectedId else null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDriverId = null
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- 2. CONFIGURACIÓN DEL RECYCLERVIEW ---
    // -------------------------------------------------------------------------
    private fun setupRecyclerView() {
        rvAssignedRequests.layoutManager = LinearLayoutManager(this)

        requestAdapter = AssignedRequestAdapter(emptyList()) { solicitudResponse ->
            fillAssignmentSection(solicitudResponse)
        }
        rvAssignedRequests.adapter = requestAdapter
    }

    // -------------------------------------------------------------------------
    // --- 3. LÓGICA DE CARGA DE DATOS Y LISTENERS ---
    // -------------------------------------------------------------------------

    private fun setupListeners() {
        btnAssignDriver.setOnClickListener {
            assignDriverToRequest()
        }
    }

    /**
     * Carga las solicitudes de la sucursal del gestor.
     */
    private fun loadRequestsForMonitoring() {
        val sucursalId = sessionManager.getSucursalId()

        // Convertir Any a Long? de forma segura
        val sucursalIdLong = (sucursalId as? String)?.toLongOrNull() ?: (sucursalId as? Long)

        if (sucursalIdLong == null || sucursalIdLong <= 0) {
            Toast.makeText(this, "Error: El gestor no tiene sucursal asignada o el ID es inválido.", Toast.LENGTH_LONG).show()
            tvNoRequests.visibility = View.VISIBLE
            rvAssignedRequests.visibility = View.GONE
            return
        }

        solicitudApi.getAssignedSolicitudesBySucursal(sucursalIdLong).enqueue(object : Callback<List<SolicitudResponse>> {
            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    if (requests.isNotEmpty()) {
                        requestAdapter.updateData(requests)
                        tvNoRequests.visibility = View.GONE
                        rvAssignedRequests.visibility = View.VISIBLE
                    } else {
                        requestAdapter.updateData(emptyList())
                        tvNoRequests.visibility = View.VISIBLE
                        rvAssignedRequests.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@AssignDriverActivity, "Error ${response.code()}: ${response.message()}", Toast.LENGTH_LONG).show()
                    requestAdapter.updateData(emptyList())
                    tvNoRequests.visibility = View.VISIBLE
                    rvAssignedRequests.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                Toast.makeText(this@AssignDriverActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                requestAdapter.updateData(emptyList())
                tvNoRequests.visibility = View.VISIBLE
                rvAssignedRequests.visibility = View.GONE
            }
        })
    }

    /**
     * Rellena la sección de asignación superior al hacer clic en un ítem pendiente de la lista.
     */
    private fun fillAssignmentSection(request: SolicitudResponse) {
        selectedSolicitudId = request.id

        if (request.estado == "PENDIENTE_ASIGNACION" || request.estado == "ASIGNADA") {
            etSolicitudId.setText(request.guia.trackingNumber)
            tvSolicitudDetails.text = "Destino: ${request.direccionCompleta}, Programada: ${request.fechaRecoleccion} (${request.franjaHoraria})"

            // 💡 Intenta preseleccionar el conductor actual si lo tiene y está en la lista del Spinner
            val currentDriverId = request.recolectorId
            if (currentDriverId != null) {
                // Busca el nombre del conductor en el mapa por su ID
                val driverDisplayName = driverIdToNameMap.entries
                    .firstOrNull { it.value == currentDriverId }?.key

                if (driverDisplayName != null) {
                    val position = driverNamesList.indexOf(driverDisplayName)
                    if (position >= 0) {
                        spinnerDrivers.setSelection(position)
                    }
                }
            } else {
                spinnerDrivers.setSelection(0)
            }
        } else {
            Toast.makeText(this, "Esta solicitud ya está en curso (${request.estado.replace("_", " ")}).", Toast.LENGTH_SHORT).show()
            etSolicitudId.text.clear()
            tvSolicitudDetails.text = ""
            selectedSolicitudId = null
        }
    }

    /**
     * Ejecuta la lógica para asignar el conductor seleccionado a la solicitud ingresada.
     */
    private fun assignDriverToRequest() {
        val solicitudId = selectedSolicitudId

        if (solicitudId == null) {
            Toast.makeText(this, "Primero, selecciona una solicitud pendiente de la lista.", Toast.LENGTH_SHORT).show()
            return
        }

        val driverId = selectedDriverId

        if (driverId == null) {
            Toast.makeText(this, "Por favor, selecciona un conductor válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // El backend espera el ID del conductor/recolector como String en el cuerpo del JSON.
        val body = mapOf("recolectorId" to driverId.toString())

        btnAssignDriver.isEnabled = false

        solicitudApi.assignRequest(solicitudId, body).enqueue(object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                btnAssignDriver.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AssignDriverActivity,
                        "Solicitud ${solicitudId} reasignada con éxito.",
                        Toast.LENGTH_LONG
                    ).show()

                    etSolicitudId.text?.clear()
                    tvSolicitudDetails.text = ""
                    selectedSolicitudId = null
                    spinnerDrivers.setSelection(0)
                    loadRequestsForMonitoring()

                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido del servidor."
                    Toast.makeText(this@AssignDriverActivity, "Error al asignar: ${response.code()} - ${errorMsg.take(50)}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                btnAssignDriver.isEnabled = true
                Toast.makeText(this@AssignDriverActivity, "Error de red en asignación: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}