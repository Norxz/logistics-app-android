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
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.Request
import com.google.android.material.button.MaterialButton
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
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
    private var driverOptions = mutableListOf<Pair<Long, String>>()
    private var selectedDriverId: Long? = null
    private var selectedStatus: String? = null
    private val statusOptions = arrayOf("PENDIENTE", "ASIGNADO", "EN RUTA", "COMPLETADO", "CANCELADO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)
        supportActionBar?.hide()

        userRepository = UserRepository(this)

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
        // Cargar conductores activos desde la base de datos
        driverOptions.clear()
        driverOptions.add(Pair(-1L, "--- Seleccionar Conductor ---")) // Opción por defecto

        val drivers = userRepository.getDriversForAssignment()
        driverOptions.addAll(drivers.map { Pair(it.first, it.second) })

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            driverOptions.map { it.second } // Mostrar solo los nombres
        )
        spinnerDrivers.adapter = adapter

        // Seleccionar conductor asignado actualmente, si existe
        val currentDriverId = currentRequest.assignedRecolectorId
        if (currentDriverId != null) {
            val index = driverOptions.indexOfFirst { it.first == currentDriverId }
            if (index != -1) {
                spinnerDrivers.setSelection(index)
                selectedDriverId = currentDriverId
            }
        }

        setupDriverSpinnerListener()
    }

    private fun setupDriverSpinnerListener() {
        spinnerDrivers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDriverId = driverOptions[position].first
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
            // TODO: Implementar el método updateRequestAssignment en UserRepository
            val success = updateRequestAssignment(currentRequest.id, selectedDriverId!!)
            if (success) {
                Toast.makeText(this, "Conductor asignado con éxito.", Toast.LENGTH_SHORT).show()
                // Recargar o actualizar la UI con la nueva data
                updateUIOnSuccess()
            } else {
                Toast.makeText(this, "Error al asignar conductor.", Toast.LENGTH_SHORT).show()
            }
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

            // TODO: Implementar el método updateRequestStatus en UserRepository
            val success = updateRequestStatus(currentRequest.id, selectedStatus!!)
            if (success) {
                Toast.makeText(this, "Estado actualizado a $selectedStatus.", Toast.LENGTH_SHORT).show()
                // Recargar o actualizar la UI con la nueva data
                updateUIOnSuccess()
            } else {
                Toast.makeText(this, "Error al actualizar estado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        setupAssignmentListener()
        setupStatusListener()
    }

    private fun updateUIOnSuccess() {
        // Idealmente, recargar la solicitud desde la BD para tener la data más fresca
        // Por simplicidad, actualizaremos los campos que cambiaron:
        currentRequest = currentRequest.copy(
            assignedRecolectorId = selectedDriverId,
            // Aquí tendrías que buscar el nombre del conductor si selectedDriverId cambió
            assignedRecolectorName = driverOptions.find { it.first == selectedDriverId }?.second,
            status = selectedStatus ?: currentRequest.status
        )
        displayRequestDetails()

        // Notificar a la lista principal (ViewAllRequestsActivity) que debe recargar sus datos
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
    private fun updateRequestAssignment(requestId: Long, recolectorId: Long): Boolean {
        // DEBES LLAMAR AL MÉTODO REAL EN userRepository
        // return userRepository.updateRequestAssignment(requestId, recolectorId)

        // Simulando éxito para que puedas probar la UI
        Log.d("RequestDetailActivity", "Simulando asignación de Request $requestId al Recolector $recolectorId")
        return true
    }

    private fun updateRequestStatus(requestId: Long, status: String): Boolean {
        // DEBES LLAMAR AL MÉTODO REAL EN userRepository
        // return userRepository.updateRequestStatus(requestId, status)

        // Simulando éxito para que puedas probar la UI
        Log.d("RequestDetailActivity", "Simulando cambio de estado de Request $requestId a $status")
        return true
    }
}