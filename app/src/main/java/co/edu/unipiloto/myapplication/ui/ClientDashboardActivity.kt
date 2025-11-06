package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton

/**
 * Activity que actúa como el panel principal para los usuarios con rol CLIENTE.
 * Muestra las solicitudes activas e historial.
 */
class ClientDashboardActivity : AppCompatActivity() {

    // --- VISTAS ---
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnNuevaSolicitud: MaterialButton

    // Controles de Solicitudes Activas
    private lateinit var tvHeaderSolicitados: TextView
    private lateinit var btnToggleSolicitados: View // ImageButton
    private lateinit var rvSolicitados: RecyclerView

    // Controles de Historial
    private lateinit var tvHeaderFinalizados: TextView
    private lateinit var btnToggleFinalizados: View // ImageButton
    private lateinit var rvFinalizados: RecyclerView

    // Mensaje de vacío
    private lateinit var tvEmpty: TextView

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    // Estado de la visibilidad
    private var isSolicitadosVisible = true
    private var isFinalizadosVisible = false // Inicialmente oculto según el layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        // Ocultar la barra de acción para usar el diseño personalizado del layout
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)

        // 1. Verificar sesión crítica
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "CLIENTE") {
            Log.w("Dashboard", "Attempted access without valid CLIENTE session. Logging out.")
            sessionManager.logoutUser()
            navigateToLoginHub()
            return
        }

        initViews()
        loadClientData()
        setupListeners()
        // loadSolicitudes() // ⚠️ Implementar en el siguiente paso
    }

    private fun initViews() {
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle)
        btnLogout = findViewById(R.id.btnLogout)
        btnNuevaSolicitud = findViewById(R.id.btnNuevaSolicitud)

        tvHeaderSolicitados = findViewById(R.id.tvHeaderSolicitados)
        btnToggleSolicitados = findViewById(R.id.btnToggleSolicitados)
        rvSolicitados = findViewById(R.id.rvSolicitados)

        tvHeaderFinalizados = findViewById(R.id.tvHeaderFinalizados)
        btnToggleFinalizados = findViewById(R.id.btnToggleFinalizados)
        rvFinalizados = findViewById(R.id.rvFinalizados)

        tvEmpty = findViewById(R.id.tvEmpty)

        // Configurar LayoutManagers para RecyclerViews
        rvSolicitados.layoutManager = LinearLayoutManager(this)
        rvFinalizados.layoutManager = LinearLayoutManager(this)

        // Asegurar que la rotación inicial del historial coincida con su visibilidad inicial (oculta)
        if (isFinalizadosVisible) btnToggleFinalizados.rotation = 0f else btnToggleFinalizados.rotation = 180f
    }

    private fun loadClientData() {
        val userId = sessionManager.getUserId()
        // Obtener el nombre para el saludo
        val clientName = userRepository.getFullNameById(userId) ?: "Cliente"

        // Personalizar el saludo con el nombre del usuario
        tvWelcomeTitle.text = getString(R.string.client_dashboard_title_welcome, clientName)
    }

    private fun setupListeners() {

        // 1. CERRAR SESIÓN
        btnLogout.setOnClickListener {
            sessionManager.logoutUser()
            navigateToLoginHub()
        }

        // 2. NUEVA SOLICITUD
        btnNuevaSolicitud.setOnClickListener {
            startActivity(Intent(this, NewDeliveryActivity::class.java))
        }

        // 3. TOGGLE Solicitudes Activas
        btnToggleSolicitados.setOnClickListener {
            // 1. Alternar el estado
            isSolicitadosVisible = !isSolicitadosVisible
            // 2. 🏆 Llamada corregida
            toggleSection(rvSolicitados, btnToggleSolicitados, isSolicitadosVisible)
        }

        // 4. TOGGLE Historial Finalizado
        btnToggleFinalizados.setOnClickListener {
            // 1. Alternar el estado
            isFinalizadosVisible = !isFinalizadosVisible
            // 2. 🏆 Llamada corregida
            toggleSection(rvFinalizados, btnToggleFinalizados, isFinalizadosVisible)
        }
    }

    /**
     * Alterna la visibilidad de un RecyclerView y cambia el ícono del botón.
     * 🏆 FUNCIÓN CORREGIDA: Ya no acepta el lambda 'setVisible'.
     *
     * @param recyclerView El RecyclerView a mostrar/ocultar.
     * @param button El ImageButton que muestra la flecha (para rotar).
     * @param newState El nuevo estado deseado (true para visible, false para oculto).
     */
    private fun toggleSection(recyclerView: RecyclerView, button: View, newState: Boolean) {
        if (newState) {
            recyclerView.visibility = View.VISIBLE
            // Flecha hacia arriba (0 grados)
            button.animate().rotation(0f).setDuration(200).start()
        } else {
            recyclerView.visibility = View.GONE
            // Flecha hacia abajo (180 grados)
            button.animate().rotation(180f).setDuration(200).start()
        }
    }

    /**
     * Redirige al usuario al Hub de Bienvenida/Login tras el logout.
     */
    private fun navigateToLoginHub() {
        val intent = Intent(this, MainActivity::class.java)
        // Estas flags borran todo el stack y ponen a MainActivity como la raíz.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ⚠️ La función loadSolicitudes() se implementará en los siguientes pasos
}