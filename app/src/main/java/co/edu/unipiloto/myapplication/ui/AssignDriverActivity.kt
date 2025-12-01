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
// Importa tus clases de Adaptador y Modelos de Datos aquí
// import co.edu.unipiloto.myapplication.data.model.Request
// import co.edu.unipiloto.myapplication.ui.adapter.AssignedRequestAdapter

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
    // private lateinit var requestAdapter: AssignedRequestAdapter // ⚠️ Descomentar al crear el Adapter
    private var selectedDriverId: String? = null
    private lateinit var driverNames: List<String> // ✨ CORRECCIÓN: Declaración a nivel de clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_driver)

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
        // Datos de Ejemplo: Mapeamos el nombre a un ID para la lógica de asignación
        val driverMap = mapOf(
            "Seleccionar Conductor" to null,
            "Juan Pérez (ID: 101)" to "DRV101",
            "María López (ID: 102)" to "DRV102",
            "Carlos Ruiz (ID: 103)" to "DRV103"
        )

        // ✨ CORRECCIÓN: Asignación a la propiedad de la clase
        driverNames = driverMap.keys.toList()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            driverNames
        )
        // El error de tipado se corrige porque 'driverNames' ahora está en un scope accesible
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDrivers.adapter = adapter

        spinnerDrivers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Ahora 'driverNames' es accesible y correctamente tipada
                selectedDriverId = driverMap[driverNames[position]]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDriverId = null
            }
        }
    }

    // --- 2. CONFIGURACIÓN DEL RECYCLERVIEW ---
    private fun setupRecyclerView() {
        rvAssignedRequests.layoutManager = LinearLayoutManager(this)

        // ⚠️ Placeholder: Descomentar al crear Request y AssignedRequestAdapter
        /*
        requestAdapter = AssignedRequestAdapter(emptyList()) { request ->
            // Manejar el clic en la solicitud pendiente para auto-rellenar la sección de asignación
            fillAssignmentSection(request)
        }
        rvAssignedRequests.adapter = requestAdapter
        */
    }

    // --- 3. LÓGICA DE CARGA DE DATOS Y LISTENERS ---

    private fun setupListeners() {
        btnAssignDriver.setOnClickListener {
            assignDriverToRequest()
        }
    }

    /**
     * Carga las solicitudes pendientes de asignar y las ya asignadas para monitoreo.
     */
    private fun loadRequestsForMonitoring() {
        // NOTA: Aquí se hace la llamada real a la API (Retrofit)

        // ⚠️ Descomentar y usar después de crear el modelo de datos (Request)
        /*
        val simulatedRequests = listOf(
             Request("GUIA451", "PENDIENTE_ASIGNACION", "BOG-Norte", "MED-Centro", null),
             Request("GUIA452", "ASIGNADA", "CAL-Sur", "BOG-Sur", "DRV101"),
             Request("GUIA453", "EN_RUTA", "MED-Oriente", "CAL-Norte", "DRV102")
        )

        if (simulatedRequests.isNotEmpty()) {
            requestAdapter.updateData(simulatedRequests)
            tvNoRequests.visibility = View.GONE
            rvAssignedRequests.visibility = View.VISIBLE
        } else {
            tvNoRequests.visibility = View.VISIBLE
            rvAssignedRequests.visibility = View.GONE
        }
        */

        // Muestra el mensaje de "No hay solicitudes" hasta que se descomente la lógica real
        tvNoRequests.visibility = View.VISIBLE
        rvAssignedRequests.visibility = View.GONE
    }

    /**
     * Rellena la sección de asignación superior al hacer clic en un ítem pendiente de la lista.
     * ⚠️ Descomentar después de crear el modelo de datos (Request)
     */
    /*
    private fun fillAssignmentSection(request: Request) {
        if (request.status == "PENDIENTE_ASIGNACION") {
            etSolicitudId.setText(request.trackingNumber)
            tvSolicitudDetails.text = "Destino: ${request.destination}, Origen: ${request.origin}"
            spinnerDrivers.setSelection(0)
        } else {
            Toast.makeText(this, "Esta solicitud ya está en curso (${request.status}).", Toast.LENGTH_SHORT).show()
        }
    }
    */

    /**
     * Ejecuta la lógica para asignar el conductor seleccionado a la solicitud ingresada.
     */
    private fun assignDriverToRequest() {
        val solicitudId = etSolicitudId.text.toString().trim()

        if (solicitudId.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID de la solicitud o haz clic en una pendiente.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDriverId == null) {
            Toast.makeText(this, "Por favor, selecciona un conductor válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⚠️ Lógica de llamada a la API de Asignación:
        // RetrofitClient.apiService.assignDriver(solicitudId, selectedDriverId!!).enqueue(...)

        // Simulación de éxito
        Toast.makeText(
            this,
            "Solicitud $solicitudId asignada a $selectedDriverId con éxito.",
            Toast.LENGTH_LONG
        ).show()

        // Limpiar la interfaz y actualizar la lista
        etSolicitudId.text?.clear()
        tvSolicitudDetails.text = ""
        spinnerDrivers.setSelection(0)
        loadRequestsForMonitoring()
    }
}