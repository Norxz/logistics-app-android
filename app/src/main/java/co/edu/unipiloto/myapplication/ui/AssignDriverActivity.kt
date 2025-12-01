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
import co.edu.unipiloto.myapplication.R
// --- IMPORTS REQUERIDOS ---
import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
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

    // Vistas del Layout (activity_assign_driver.xml)
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
    private lateinit var driverNames: List<String>

    // Servicios y Gestores
    private lateinit var solicitudApi: SolicitudApi
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_driver)

        // 🚨 CORRECCIÓN DE REFERENCIA: Uso del patrón Retrofit.create()
        // Asumiendo que RetrofitClient.getInstance() devuelve el objeto Retrofit configurado
        solicitudApi = RetrofitClient.getSolicitudApi()
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

    // --- 1. CONFIGURACIÓN DEL SPINNER DE CONDUCTORES ---
    private fun setupDriverSpinner() {
        val driverMap = mapOf(
            "Seleccionar Conductor" to null,
            "Juan Pérez (ID: 101)" to "101",
            "María López (ID: 102)" to "102",
            "Carlos Ruiz (ID: 103)" to "103"
        )

        driverNames = driverMap.keys.toList()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            driverNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDrivers.adapter = adapter

        spinnerDrivers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDriverId = driverMap[driverNames[position]]?.toLongOrNull()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDriverId = null
            }
        }
    }

    // --- 2. CONFIGURACIÓN DEL RECYCLERVIEW ---
    private fun setupRecyclerView() {
        rvAssignedRequests.layoutManager = LinearLayoutManager(this)

        requestAdapter = AssignedRequestAdapter(emptyList()) { solicitudResponse ->
            fillAssignmentSection(solicitudResponse)
        }
        rvAssignedRequests.adapter = requestAdapter
    }

    // --- 3. LÓGICA DE CARGA DE DATOS Y LISTENERS ---

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

        // Si getSucursalId devuelve String:
        val sucursalIdLong = if (sucursalId is Long) sucursalId else (sucursalId as? String)?.toLongOrNull()

        if (sucursalIdLong == null) {
            Toast.makeText(this, "Error: El gestor no tiene sucursal asignada o el ID es inválido.", Toast.LENGTH_LONG).show()
            tvNoRequests.visibility = View.VISIBLE
            rvAssignedRequests.visibility = View.GONE
            return
        }

        // 🚨 CORRECCIÓN CLAVE: Cambiar a getAssignedSolicitudesBySucursal para monitoreo
        solicitudApi.getAssignedSolicitudesBySucursal(sucursalIdLong).enqueue(object : Callback<List<SolicitudResponse>> {
            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    if (requests.isNotEmpty()) {
                        // Filtramos las solicitudes que están PENDIENTES DE ASIGNACIÓN
                        // para que el gestor pueda hacer clic sobre ellas y reasignar o ver detalles.
                        // (Si solo quieres ver las que ya están EN CURSO, puedes aplicar un filtro aquí)
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

        if (request.estado == "PENDIENTE_ASIGNACION") {
            etSolicitudId.setText(request.guia.trackingNumber)
            tvSolicitudDetails.text = "Destino: ${request.direccionCompleta}, Programada: ${request.fechaRecoleccion} (${request.franjaHoraria})"
            spinnerDrivers.setSelection(0)
        } else {
            Toast.makeText(this, "Esta solicitud ya está en curso (${request.estado.replace("_", " ")}).", Toast.LENGTH_SHORT).show()
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
                        "Solicitud ${solicitudId} asignada con éxito.",
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