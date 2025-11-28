package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity para el panel de control (dashboard) del conductor.
 */
class DriverDashboardActivity : AppCompatActivity() {

    // --- VISTAS ---
    private lateinit var tvDriverTitle: TextView
    private lateinit var tvDriverSubtitle: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var tvNoRoutes: TextView

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SolicitudAdapter

    // --- DATOS DE SESIÓN ---
    private var driverId: Long = -1L
    private var driverZona: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        supportActionBar?.hide()

        // Inicializar gestores
        sessionManager = SessionManager(this)

        // Verificar si el usuario está logueado y tiene el rol correcto.
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "CONDUCTOR") {
            logoutUser()
            return
        }

        // Obtener datos del conductor de la sesión actual
        driverId = sessionManager.getUserId()
        driverZona = sessionManager.getZona()

        // Configurar la UI y cargar los datos
        initViews()
        setupListeners()
        setupRecyclerView()
        loadAssignedRoutes() // 👈 Llamada para cargar rutas
    }

    override fun onResume() {
        super.onResume()
        // Asegura que las rutas se recarguen al volver de una acción (ej., marcar recogida).
        loadAssignedRoutes()
    }

    private fun initViews() {
        tvDriverTitle = findViewById(R.id.tvDriverTitle)
        tvDriverSubtitle = findViewById(R.id.tvDriverSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)

        val driverName = sessionManager.getName().split(" ").firstOrNull() ?: "Conductor"
        tvDriverTitle.text = getString(R.string.driver_dashboard_title, driverName)
        tvDriverSubtitle.text =
            getString(R.string.driver_dashboard_subtitle, driverZona ?: "Sin Zona")
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupRecyclerView() {
        // Inicializamos el adaptador con el rol CONDUCTOR para que muestre los botones correctos
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = sessionManager.getRole()
            // Aquí iría el listener para manejar acciones del conductor (INICIAR, RECOGIDA, ENTREGADA)
            // onActionClick = { solicitud, action -> handleDriverAction(solicitud, action) }
        )

        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.adapter = adapter
    }

    /**
     * Carga las rutas (solicitudes) asignadas al conductor usando el servicio REST.
     */
    private fun loadAssignedRoutes() {
        if (driverId == -1L) {
            Toast.makeText(this, "Error: ID de conductor no válido.", Toast.LENGTH_LONG).show()
            return
        }

        // 🏆 LLAMADA A RETROFIT
        RetrofitClient.apiService.getDriverRoutes(driverId).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val assignedSolicitudes = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    if (assignedSolicitudes.isNotEmpty()) {
                        adapter.updateData(assignedSolicitudes)
                        recyclerViewRoutes.visibility = View.VISIBLE
                        tvNoRoutes.visibility = View.GONE
                    } else {
                        recyclerViewRoutes.visibility = View.GONE
                        tvNoRoutes.visibility = View.VISIBLE
                        tvNoRoutes.text = getString(R.string.no_routes_assigned) // Asegúrate de tener este string
                    }
                } else {
                    Log.e("DriverDash", "Error ${response.code()} al cargar rutas.")
                    Toast.makeText(this@DriverDashboardActivity, "Error al cargar rutas del servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("DriverDash", "Fallo de red: ${t.message}")
                Toast.makeText(this@DriverDashboardActivity, "Fallo de red: No se pudo conectar al backend.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}