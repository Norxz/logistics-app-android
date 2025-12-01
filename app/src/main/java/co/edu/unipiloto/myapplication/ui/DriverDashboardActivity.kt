package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.DriverRequestAdapter
import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.adapters.OnRequestClickListener
import retrofit2.Response

/**
 * Actividad principal del conductor. Muestra las solicitudes (rutas) asignadas
 * y permite actualizar su estado.
 */
class DriverDashboardActivity : AppCompatActivity(), OnRequestClickListener {

    private lateinit var tvDriverTitle: TextView
    private lateinit var tvDriverSubtitle: TextView
    private lateinit var btnLogout: Button
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var tvNoRoutes: TextView
    private lateinit var requestAdapter: DriverRequestAdapter

    private lateinit var solicitudApi: SolicitudApi
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        // 🚨 CORRECCIÓN: Inicializar servicios (sessionManager) antes de usar las vistas
        initServices()
        initViews()

        setupListeners()
        setupRecyclerView()
        loadAssignedRequests()
    }

    private fun initViews() {
        tvDriverTitle = findViewById(R.id.tvDriverTitle)
        tvDriverSubtitle = findViewById(R.id.tvDriverSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)

        // Ahora sessionManager ya está inicializado.
        tvDriverTitle.text = getString(R.string.driver_dashboard_title, sessionManager.getUserFullName() ?: "Conductor")
        tvDriverSubtitle.text = getString(R.string.driver_dashboard_subtitle)
    }

    private fun initServices() {
        solicitudApi = RetrofitClient.getSolicitudApi()
        sessionManager = SessionManager(this) // Se inicializa aquí
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        requestAdapter = DriverRequestAdapter(emptyList(), this)
        recyclerViewRoutes.adapter = requestAdapter
    }

    // --- LÓGICA DE CARGA DE DATOS (COROUTINES) ---

    /**
     * Carga las solicitudes asignadas al ID del conductor logueado.
     */
    private fun loadAssignedRequests() {
        val driverId = sessionManager.getUserId()

        if (driverId == -1L) {
            Toast.makeText(this, "Error: ID de conductor no válido.", Toast.LENGTH_LONG).show()
            tvNoRoutes.visibility = View.VISIBLE
            return
        }

        lifecycleScope.launch {
            try {
                // Se asume que SolicitudApi.kt fue modificado para incluir
                // suspend fun getRoutesByDriverIdCoroutines(...)
                val response: Response<List<SolicitudResponse>> = solicitudApi.getRoutesByDriverIdCoroutines(driverId)

                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    if (requests.isNotEmpty()) {
                        requestAdapter.updateData(requests)
                        tvNoRoutes.visibility = View.GONE
                        recyclerViewRoutes.visibility = View.VISIBLE
                    } else {
                        requestAdapter.updateData(emptyList())
                        tvNoRoutes.visibility = View.VISIBLE
                        recyclerViewRoutes.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@DriverDashboardActivity, "Error ${response.code()} al cargar rutas.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverDashboardActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- LÓGICA DE ACTUALIZACIÓN DE ESTADO (COROUTINES) ---

    /**
     * Implementación de OnRequestClickListener. Llamada cuando se hace clic en un botón de estado.
     */
    override fun onRequestStatusChange(solicitudId: Long, currentStatus: String) {
        val nextStatus = getNextStatus(currentStatus)

        if (nextStatus == null) {
            Toast.makeText(this, "La solicitud ya ha sido completada o cancelada.", Toast.LENGTH_SHORT).show()
            return
        }

        updateRequestStatus(solicitudId, nextStatus)
    }

    /**
     * Define la transición de estados para el conductor.
     */
    private fun getNextStatus(currentStatus: String): String? {
        return when (currentStatus) {
            "ASIGNADA" -> "EN_RUTA_RECOLECCION"
            "EN_RUTA_RECOLECCION" -> "EN_DISTRIBUCION"
            "EN_DISTRIBUCION" -> "EN_RUTA_REPARTO"
            "EN_RUTA_REPARTO" -> "ENTREGADA"
            else -> null
        }
    }

    /**
     * Llama a la API para actualizar el estado de la solicitud.
     */
    private fun updateRequestStatus(solicitudId: Long, newStatus: String) {
        val body = mapOf("estado" to newStatus)

        lifecycleScope.launch {
            try {
                val response: Response<Void> = solicitudApi.updateEstado(solicitudId, body)

                if (response.isSuccessful || response.code() == 204) {
                    Toast.makeText(this@DriverDashboardActivity, "Estado actualizado a $newStatus.", Toast.LENGTH_SHORT).show()
                    loadAssignedRequests()
                } else {
                    Toast.makeText(this@DriverDashboardActivity, "Error ${response.code()} al actualizar estado.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverDashboardActivity, "Error de red al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}