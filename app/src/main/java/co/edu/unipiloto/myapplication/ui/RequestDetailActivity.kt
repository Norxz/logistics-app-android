package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.model.Request
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.model.LogisticUser
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var currentRequest: Request

    // Vistas
    private lateinit var tvDetailGuiaID: TextView
    private lateinit var tvDetailStatus: TextView
    private lateinit var tvAssignedDriver: TextView
    private lateinit var tvDetailAddress: TextView
    private lateinit var tvDetailClient: TextView
    private lateinit var tvDetailCreated: TextView

    private lateinit var spinnerDrivers: Spinner
    private lateinit var btnSaveAssignment: MaterialButton
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSaveStatus: MaterialButton

    // Datos
    private lateinit var driverOptionsList: List<LogisticUser>
    private var selectedDriverId: Long? = null
    private var selectedStatus: String? = null
    private val statusOptions = arrayOf("PENDIENTE", "ASIGNADO", "EN RUTA", "COMPLETADO", "CANCELADO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)
        supportActionBar?.hide()

        // 1. Cargar el objeto Request
        val requestData = intent.getSerializableExtra("REQUEST_DATA")
        if (requestData is Request) {
            currentRequest = requestData
        } else {
            Toast.makeText(this, "Error: No se encontró la solicitud.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        displayRequestDetails()
        loadDrivers()
        setupListeners()
    }

    private fun initViews() {
        // Inicialización de Vistas de Detalle
        tvDetailGuiaID = findViewById(R.id.tvDetailGuiaID)
        tvDetailStatus = findViewById(R.id.tvDetailStatus)
        tvAssignedDriver = findViewById(R.id.tvAssignedDriver)
        tvDetailAddress = findViewById(R.id.tvDetailAddress)
        tvDetailClient = findViewById(R.id.tvDetailClient)
        tvDetailCreated = findViewById(R.id.tvDetailCreated)

        // Inicialización de Vistas de Gestión
        spinnerDrivers = findViewById(R.id.spinnerDrivers)
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSaveStatus = findViewById(R.id.btnSaveStatus)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun displayRequestDetails() {
        tvDetailGuiaID.text = "Guía: #${currentRequest.guiaId}"
        tvDetailStatus.text = "Estado Actual: ${currentRequest.status}"

        val driverName = currentRequest.assignedRecolectorName ?: "(Aún no asignado)"
        tvAssignedDriver.text = "Asignado a: $driverName"

        tvDetailAddress.text = "Dirección: ${currentRequest.address}"

        val clientInfo = "${currentRequest.clientName} (${currentRequest.clientPhone ?: "N/A"})"
        tvDetailClient.text = "Cliente: $clientInfo"

        tvDetailCreated.text = formatTimestamp(currentRequest.creationTimestamp)

        // Sincronizar spinners con el estado actual
        setupStatusSpinner()
    }

    // ==========================================================
    // LÓGICA DE ASIGNACIÓN DE CONDUCTOR
    // ==========================================================

    private fun loadDrivers() {
        val defaultOption = "--- Seleccionar Conductor ---"
        val driverNames = mutableListOf(defaultOption)

        // 🏆 LLAMADA A RETROFIT (GET Drivers)
        RetrofitClient.apiService.getDriversForAssignment().enqueue(object : Callback<List<LogisticUser>> {
            override fun onResponse(call: Call<List<LogisticUser>>, response: Response<List<LogisticUser>>) {
                if (response.isSuccessful && response.body() != null) {
                    // 🚨 CORRECCIÓN: Inicializar la lista global de opciones
                    driverOptionsList = response.body()!!

                    // 1. Llenar la lista de nombres para el Spinner
                    driverOptionsList.forEach { driverNames.add(it.name) }

                    val adapter = ArrayAdapter(
                        this@RequestDetailActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        driverNames
                    )
                    spinnerDrivers.adapter = adapter

                    // 2. Sincronizar selección
                    selectCurrentDriver(adapter)

                } else {
                    Toast.makeText(this@RequestDetailActivity, "Error al cargar conductores.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<LogisticUser>>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al cargar conductores.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun selectCurrentDriver(adapter: ArrayAdapter<String>) {
        val currentDriverId = currentRequest.assignedRecolectorId
        if (currentDriverId != null) {
            // 🚨 CORRECCIÓN: Buscar en la lista REST cargada (driverOptionsList)
            val index = driverOptionsList.indexOfFirst { it.id == currentDriverId }
            if (index != -1) {
                // El índice en el Spinner es 1 + el índice de la lista real (por la opción por defecto)
                spinnerDrivers.setSelection(index + 1)
                selectedDriverId = currentDriverId
            }
        }
        setupDriverSpinnerListener()
    }

    private fun setupDriverSpinnerListener() {
        spinnerDrivers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    // 🚨 CORRECCIÓN: Mapear la posición a la lista REST cargada
                    selectedDriverId = driverOptionsList[position - 1].id
                } else {
                    selectedDriverId = null // Opción por defecto
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDriverId = null
            }
        }
    }

    private fun setupAssignmentListener() {
        btnSaveAssignment.setOnClickListener {
            if (selectedDriverId == null || selectedDriverId == -1L) {
                Toast.makeText(this, "Seleccione un conductor válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 🏆 LLAMADA REST para Asignación
            updateRequestAssignment(currentRequest.id, selectedDriverId!!)
        }
    }

    // ==========================================================
    // LÓGICA DE CAMBIO DE ESTADO
    // ==========================================================

    private fun setupStatusSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusOptions
        )
        spinnerStatus.adapter = adapter

        // Seleccionar estado actual
        val currentIndex = statusOptions.indexOf(currentRequest.status)
        if (currentIndex != -1) {
            spinnerStatus.setSelection(currentIndex)
            selectedStatus = currentRequest.status
        }

        setupStatusSpinnerListener()
    }

    private fun setupStatusSpinnerListener() {
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedStatus = statusOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }
    }

    private fun setupStatusListener() {
        btnSaveStatus.setOnClickListener {
            if (selectedStatus == null || selectedStatus == currentRequest.status) {
                Toast.makeText(this, "Seleccione un estado diferente para actualizar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🏆 LLAMADA REST para Actualización de Estado
            updateRequestStatus(currentRequest.id, selectedStatus!!)
        }
    }

    private fun setupListeners() {
        setupAssignmentListener()
        setupStatusListener()
    }

    private fun updateUIOnSuccess() {
        // 🏆 CORRECCIÓN CRÍTICA: Actualizar la Request con el nombre del conductor seleccionado.
        val newDriverName = driverOptionsList.find { it.id == selectedDriverId }?.name

        currentRequest = currentRequest.copy(
            assignedRecolectorId = selectedDriverId,
            assignedRecolectorName = newDriverName,
            status = selectedStatus ?: currentRequest.status
        )
        displayRequestDetails()

        setResult(RESULT_OK)
    }

    private fun formatTimestamp(timestamp: String): String {
        // Intenta formatear la marca de tiempo de la BD (si está en formato ISO)
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            "Creada el: ${outputFormat.format(date)}"
        } catch (e: Exception) {
            Log.e("RequestDetail", "Error formatting timestamp: ${e.message}")
            "Creada el: ${timestamp.substringBefore(" ")}"
        }
    }

    // ==========================================================
    // MÉTODOS SIMULADOS DE REPOSITORY (DEBEN SER IMPLEMENTADOS)
    // ==========================================================

    /**
     * IMPORTANTE: Estos métodos deben ser añadidos a tu UserRepository.kt
     */
// En RequestDetailActivity.kt

    private fun updateRequestAssignment(requestId: Long, recolectorId: Long) {
        val assignmentBody = mapOf("recolectorId" to recolectorId.toString())

        // 🏆 CORRECCIÓN: Cambiar Callback<Void> a Callback<Solicitud>
        RetrofitClient.apiService.assignRequest(requestId, assignmentBody).enqueue(object : Callback<Solicitud> {

            // El método onResponse ahora espera una Solicitud
            override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {

                if (response.isSuccessful) {
                    // Opcional: Usar el objeto Solicitud actualizado si lo necesitas:
                    // val updatedSolicitud = response.body()

                    Toast.makeText(this@RequestDetailActivity, "Conductor asignado con éxito.", Toast.LENGTH_SHORT).show()
                    updateUIOnSuccess()
                } else {
                    Toast.makeText(this@RequestDetailActivity, "Error al asignar conductor. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            // El método onFailure ahora espera Solicitud
            override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al asignar conductor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateRequestStatus(requestId: Long, status: String) {
        val statusBody = mapOf("estado" to status)

        // 🏆 CORRECCIÓN 2: Usar Callback<Void> directamente
        RetrofitClient.apiService.actualizarEstado(requestId, statusBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RequestDetailActivity, "Estado actualizado a $status.", Toast.LENGTH_SHORT).show()
                    updateUIOnSuccess()
                } else {
                    // Si falla, no hay cuerpo que leer (response.errorBody().string())
                    Toast.makeText(this@RequestDetailActivity, "Error al actualizar estado. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al actualizar estado.", Toast.LENGTH_LONG).show()
            }
        })
    }
}