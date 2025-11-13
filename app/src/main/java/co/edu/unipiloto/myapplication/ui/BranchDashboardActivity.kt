package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter // 👈 Necesario
import co.edu.unipiloto.myapplication.models.Solicitud // 👈 Modelo de Respuesta REST
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 Cliente REST

/**
 * Activity para el Dashboard del Funcionario de Sucursal.
 * Muestra listas colapsables de solicitudes PENDIENTES, EN RUTA e HISTORIAL.
 */
class BranchDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // Vistas y Adaptadores
    private lateinit var rvPending: RecyclerView
    private lateinit var rvInRoute: RecyclerView
    private lateinit var rvCompleted: RecyclerView

    private lateinit var pendingAdapter: SolicitudAdapter
    private lateinit var inRouteAdapter: SolicitudAdapter
    private lateinit var completedAdapter: SolicitudAdapter

    // El rol del usuario (CONDUCTOR, GESTOR) se usa para definir las acciones en los adaptadores
    private lateinit var userRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_functionary_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getRole() !in listOf("FUNCIONARIO", "GESTOR")) {
            logoutUser()
            return
        }

        userRole = sessionManager.getRole()

        initViews() // Mapear vistas
        setupRecyclerAndToggles()
        setupListeners()
        // 🚨 La carga real de datos (loadRequests()) debe ocurrir aquí o en onResume
    }

    override fun onResume() {
        super.onResume()
        // 🏆 Esto asegura que los Fragments (si los hubieras usado) o la Activity se actualicen.
        // Como estamos usando RecyclerViews directamente aquí, llamaríamos a loadAllRequests()
    }

    private fun initViews() {
        rvPending = findViewById(R.id.rvPending)
        rvInRoute = findViewById(R.id.rvInRoute)
        rvCompleted = findViewById(R.id.rvCompleted)

        // ... (el resto de vistas)
    }

    private fun setupRecyclerAndToggles() {
        val btnTogglePending: ImageButton = findViewById(R.id.btnTogglePending)
        val btnToggleInRoute: ImageButton = findViewById(R.id.btnToggleInRoute)
        val btnToggleCompleted: ImageButton = findViewById(R.id.btnToggleCompleted)

        // 1. Configurar LayoutManagers
        rvPending.layoutManager = LinearLayoutManager(this)
        rvInRoute.layoutManager = LinearLayoutManager(this)
        rvCompleted.layoutManager = LinearLayoutManager(this)

        // 2. 🏆 Inicializar y Asignar Adaptadores (Usando el rol actual para definir botones)
        pendingAdapter = SolicitudAdapter(emptyList(), userRole) { solicitud, action -> handleAction(solicitud, action) }
        inRouteAdapter = SolicitudAdapter(emptyList(), userRole) { solicitud, action -> handleAction(solicitud, action) }
        completedAdapter = SolicitudAdapter(emptyList(), userRole)

        rvPending.adapter = pendingAdapter
        rvInRoute.adapter = inRouteAdapter
        rvCompleted.adapter = completedAdapter

        // Lógica de Toggle
        btnTogglePending.setOnClickListener { toggleVisibility(rvPending, btnTogglePending, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }
        btnToggleInRoute.setOnClickListener { toggleVisibility(rvInRoute, btnToggleInRoute, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }
        btnToggleCompleted.setOnClickListener { toggleVisibility(rvCompleted, btnToggleCompleted, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }

        if (rvPending.visibility == View.VISIBLE) {
            btnTogglePending.setImageResource(R.drawable.ic_arrow_up)
        }
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            logoutUser()
        }

        findViewById<MaterialButton>(R.id.btnNewRequest).setOnClickListener {
            // Ir al proceso de solicitud
            startActivity(Intent(this, SolicitudActivity::class.java))
        }
    }

    // 🏆 NUEVA FUNCIÓN: Manejar acciones de los botones (e.g., ASIGNAR)
    private fun handleAction(solicitud: Solicitud, action: String) {
        // Aquí iría la lógica de PUT/POST a tu backend para actualizar estados o asignar conductores.
        // Ej: RetrofitClient.apiService.assignRequest(solicitud.id, { "recolectorId": 5 })
        if (action == "ASIGNAR") {
            // Lógica de asignación (debes implementar el endpoint en el backend)
            // Toast.makeText(this, "Simulando asignación de ${solicitud.id}", Toast.LENGTH_SHORT).show()
        }
    }

    // ... (toggleVisibility y logoutUser se mantienen)

    private fun toggleVisibility(rv: RecyclerView, btn: ImageButton, iconUp: Int, iconDown: Int) {
        if (rv.visibility == View.VISIBLE) {
            rv.visibility = View.GONE
            btn.setImageResource(iconDown)
        } else {
            rv.visibility = View.VISIBLE
            btn.setImageResource(iconUp)
        }
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}