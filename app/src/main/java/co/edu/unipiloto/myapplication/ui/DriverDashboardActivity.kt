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
import co.edu.unipiloto.myapplication.db.SolicitudRepository
// import co.edu.unipiloto.myapplication.db.UserRepository // Ya no es estrictamente necesario aquí
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter // Asumimos esta será la ruta
import com.google.android.material.button.MaterialButton

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
    // private lateinit var userRepository: UserRepository // Ya no se necesita aquí
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var adapter: SolicitudAdapter

    // --- DATOS DE SESIÓN ---
    private var driverId: Long = -1L
    private var driverZona: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nota: Asumo que tienes un layout llamado activity_driver_dashboard
        setContentView(R.layout.activity_driver_dashboard)

        supportActionBar?.hide()

        // Inicializar gestores y repositorios
        sessionManager = SessionManager(this)
        // Nota: Necesitas la clase SolicitudRepository para que esto compile.
        solicitudRepository = SolicitudRepository(this)
        // userRepository = UserRepository(this) // Ya no es necesario

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
        //loadAssignedRoutes() // Se ejecutará cuando SolicitudRepository esté listo
    }

    /**
     * Inicializa y mapea las vistas. Personaliza los textos de bienvenida
     * obteniendo el nombre directamente del SessionManager.
     */
    private fun initViews() {
        tvDriverTitle = findViewById(R.id.tvDriverTitle)
        tvDriverSubtitle = findViewById(R.id.tvDriverSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)

        // 🏆 AJUSTE CRÍTICO: Obtener el nombre del SessionManager (es más rápido y eficiente)
        // Usamos el primer nombre para un saludo más casual.
        val driverName = sessionManager.getName().split(" ").firstOrNull() ?: "Conductor"

        // Personaliza el título con el nombre real.
        tvDriverTitle.text = getString(R.string.driver_dashboard_title, driverName)

        // Muestra la zona asignada o un texto por defecto.
        tvDriverSubtitle.text =
            getString(R.string.driver_dashboard_subtitle, driverZona ?: "Sin Zona")
    }

    /**
     * Configura los listeners.
     */
    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Configura el RecyclerView.
     */
    private fun setupRecyclerView() {
        // ⚠️ Nota: Necesitas crear el SolicitudAdapter y el modelo Solicitud
        // Antes de que esta línea compile:
        // adapter = SolicitudAdapter.forConductor(items = emptyList())

        // Mientras tanto, usaremos una inicialización básica para que compile:
        // Asegúrate de crear esta clase pronto:
        // adapter = SolicitudAdapter(emptyList())

        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        // recyclerViewRoutes.adapter = adapter
    }

    /**
     * Carga las rutas (solicitudes) asignadas al conductor.
     */
    private fun loadAssignedRoutes() {
        if (driverId == -1L) {
            Toast.makeText(this, "Error: ID de conductor no válido.", Toast.LENGTH_LONG).show()
            return
        }

        // ⚠️ Esta línea requiere que SolicitudRepository esté implementado:
        // val assignedSolicitudes = solicitudRepository.getSolicitudesByRecolectorId(driverId)

        // if (assignedSolicitudes.isNotEmpty()) {
        //     adapter.updateData(assignedSolicitudes)
        //     recyclerViewRoutes.visibility = View.VISIBLE
        //     tvNoRoutes.visibility = View.GONE
        // } else {
        //     recyclerViewRoutes.visibility = View.GONE
        //     tvNoRoutes.visibility = View.VISIBLE
        // }
    }

    /**
     * Cierra la sesión y redirige al Hub de Bienvenida (MainActivity).
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        // 🏆 AJUSTE: Redirigir a MainActivity (Hub) para consistencia
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}