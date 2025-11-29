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
// Importaciones de Modelos
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.User
import com.google.android.material.button.MaterialButton
// Importación del Cliente Retrofit desde el paquete correcto
import co.edu.unipiloto.myapplication.dto.RetrofitClient.getSolicitudApi
import co.edu.unipiloto.myapplication.dto.RetrofitClient.getUserApi
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.io.Serializable // Asegúrate de que Solicitud sea Serializable

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var currentRequest: Solicitud
    private var newAssignedDriver: User? = null // Variable temporal para el conductor recién asignado

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
    private var driverOptionsList: List<User> = emptyList()
    private var selectedDriverId: Long? = null
    private var selectedStatus: String? = null
    private val statusOptions = arrayOf("PENDIENTE", "ASIGNADO", "EN RUTA", "COMPLETADO", "CANCELADO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)
        supportActionBar?.hide()

        // 1. Cargar el objeto Solicitud
        val requestData = intent.getSerializableExtra("REQUEST_DATA")
        if (requestData is Solicitud) {
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
        tvDetailGuiaID = findViewById(R.id.tvDetailGuiaID)
        tvDetailStatus = findViewById(R.id.tvDetailStatus)
        tvAssignedDriver = findViewById(R.id.tvAssignedDriver)
        tvDetailAddress = findViewById(R.id.tvDetailAddress)
        tvDetailClient = findViewById(R.id.tvDetailClient)
        tvDetailCreated = findViewById(R.id.tvDetailCreated)

        spinnerDrivers = findViewById(R.id.spinnerDrivers)
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSaveStatus = findViewById(R.id.btnSaveStatus)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun displayRequestDetails() {
        // ✅ CORRECCIÓN 1: Usamos 'id' en lugar de 'idGuia' (asumiendo que 'id' es el campo correcto)
        tvDetailGuiaID.text = "Guía: #${currentRequest.guia.id}"
        tvDetailStatus.text = "Estado Actual: ${currentRequest.estado}"

        val driverName = currentRequest.conductor?.fullName ?: "(Aún no asignado)"
        tvAssignedDriver.text = "Asignado a: $driverName"

        // Usamos la dirección de Recolección. Si es null, mostramos la de Entrega.
        val addressText = currentRequest.direccionRecoleccion?.direccionCompleta
            ?: currentRequest.direccionEntrega.direccionCompleta
        tvDetailAddress.text = "Dirección: $addressText"

        // ✅ CORRECCIÓN 2: Usamos 'phoneNumber' en lugar de 'phone' (asumiendo que es el campo correcto en User)
        val clientInfo = "${currentRequest.client.fullName} (${currentRequest.client.phoneNumber ?: "N/A"})"
        tvDetailClient.text = "Cliente: $clientInfo"

        tvDetailCreated.text = formatTimestamp(currentRequest.createdAt)

        setupStatusSpinner()
    }

    // ==========================================================
    // LÓGICA DE ASIGNACIÓN DE CONDUCTOR
    // ==========================================================

    private fun loadDrivers() {
        val defaultOption = "--- Seleccionar Conductor ---"
        val driverNames = mutableListOf(defaultOption)

        // ✅ CORRECCIÓN 3: Llamada a getUserApi().getDriversForAssignment()
        getUserApi().getDriversForAssignment().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    driverOptionsList = response.body()!!
                    driverOptionsList.forEach { driverNames.add(it.fullName) }

                    val adapter = ArrayAdapter(
                        this@RequestDetailActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        driverNames
                    )
                    spinnerDrivers.adapter = adapter
                    selectCurrentDriver(adapter)
                } else {
                    Toast.makeText(this@RequestDetailActivity, "Error al cargar conductores.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al cargar conductores.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun selectCurrentDriver(adapter: ArrayAdapter<String>) {
        val currentDriverId = currentRequest.conductor?.id
        if (currentDriverId != null) {
            val index = driverOptionsList.indexOfFirst { it.id == currentDriverId }
            if (index != -1) {
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
                    val driver = driverOptionsList[position - 1]
                    selectedDriverId = driver.id
                    newAssignedDriver = driver
                } else {
                    selectedDriverId = null
                    newAssignedDriver = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDriverId = null
                newAssignedDriver = null
            }
        }
    }

    private fun setupAssignmentListener() {
        btnSaveAssignment.setOnClickListener {
            if (selectedDriverId == null || currentRequest.id == null) {
                Toast.makeText(this, "Seleccione un conductor válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateRequestAssignment(currentRequest.id!!, selectedDriverId!!)
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

        val currentIndex = statusOptions.indexOf(currentRequest.estado)
        if (currentIndex != -1) {
            spinnerStatus.setSelection(currentIndex)
            selectedStatus = currentRequest.estado
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
            if (selectedStatus == null || selectedStatus == currentRequest.estado || currentRequest.id == null) {
                Toast.makeText(this, "Seleccione un estado diferente para actualizar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateRequestStatus(currentRequest.id!!, selectedStatus!!)
        }
    }

    private fun setupListeners() {
        setupAssignmentListener()
        setupStatusListener()
    }

    private fun updateUIOnSuccess() {
        // Se usa copy() para actualizar el objeto conductor (User) y el campo estado
        currentRequest = currentRequest.copy(
            conductor = newAssignedDriver ?: currentRequest.conductor,
            estado = selectedStatus ?: currentRequest.estado
        )
        displayRequestDetails()

        setResult(RESULT_OK)
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            "Creada el: ${outputFormat.format(date)}"
        } catch (e: Exception) {
            Log.e("RequestDetail", "Error formatting timestamp: ${e.message}")
            "Creada el: ${timestamp.substringBefore(" ")}"
        }
    }

    // ==========================================================
    // MÉTODOS DE RETROFIT
    // ==========================================================

    private fun updateRequestAssignment(requestId: Long, recolectorId: Long) {
        val assignmentBody = mapOf("recolectorId" to recolectorId.toString())

        // ✅ CORRECCIÓN: El Callback debe ser consistente con el tipo devuelto por tu SolicitudApi (SolicitudResponse)
        getSolicitudApi().assignRequest(requestId, assignmentBody).enqueue(object : Callback<SolicitudResponse> {

            // ✅ CORRECCIÓN: onResponse debe coincidir con el tipo del Callback (SolicitudResponse)
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RequestDetailActivity, "Conductor asignado con éxito.", Toast.LENGTH_SHORT).show()

                    // NOTA: Si SolicitudResponse contiene la Solicitud actualizada, debes extraerla aquí.
                    // Por simplicidad, se mantiene la lógica de actualización local:
                    // response.body()?.solicitud?.let { updatedRequest -> currentRequest = updatedRequest }

                    updateUIOnSuccess()
                } else {
                    Toast.makeText(this@RequestDetailActivity, "Error al asignar conductor. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            // ✅ CORRECCIÓN: onFailure debe coincidir con el tipo del Callback (SolicitudResponse)
            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al asignar conductor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateRequestStatus(requestId: Long, status: String) {
        val statusBody = mapOf("estado" to status)

        getSolicitudApi().actualizarEstado(requestId, statusBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RequestDetailActivity, "Estado actualizado a $status.", Toast.LENGTH_SHORT).show()
                    selectedStatus = status
                    updateUIOnSuccess()
                } else {
                    Toast.makeText(this@RequestDetailActivity, "Error al actualizar estado. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RequestDetailActivity, "Fallo de red al actualizar estado.", Toast.LENGTH_LONG).show()
            }
        })
    }
}