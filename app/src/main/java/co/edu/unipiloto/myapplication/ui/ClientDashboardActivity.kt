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
import com.google.android.material.imageview.ShapeableImageView

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
    private var isFinalizadosVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

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
        // 🏆 CORRECCIÓN: Obtener el nombre directamente del SessionManager
        loadClientDataFromSession()
        setupListeners()
        // loadSolicitudes()
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

        // Configurar visibilidad inicial
        rvSolicitados.visibility = if (isSolicitadosVisible) View.VISIBLE else View.GONE
        rvFinalizados.visibility = if (isFinalizadosVisible) View.VISIBLE else View.GONE

        // Configurar rotación inicial
        btnToggleSolicitados.rotation = if (isSolicitadosVisible) 0f else 180f
        btnToggleFinalizados.rotation = if (isFinalizadosVisible) 0f else 180f
    }

    /**
     * 🏆 CORRECCIÓN: Obtiene el nombre del usuario directamente de SessionManager
     * (donde fue guardado en LoginActivity) para evitar una consulta extra a la BD.
     */
    private fun loadClientDataFromSession() {
        // Obtener el nombre guardado en LoginActivity
        val clientName = sessionManager.getName().split(" ").firstOrNull() ?: "Cliente"

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
            isSolicitadosVisible = !isSolicitadosVisible
            toggleSection(rvSolicitados, btnToggleSolicitados, isSolicitadosVisible)
        }

        // 4. TOGGLE Historial Finalizado
        btnToggleFinalizados.setOnClickListener {
            isFinalizadosVisible = !isFinalizadosVisible
            toggleSection(rvFinalizados, btnToggleFinalizados, isFinalizadosVisible)
        }
    }

    /**
     * Alterna la visibilidad de un RecyclerView y anima la rotación del botón.
     */
    private fun toggleSection(recyclerView: RecyclerView, button: View, newState: Boolean) {
        if (newState) {
            recyclerView.visibility = View.VISIBLE
            // Flecha hacia arriba (ícono abierto)
            button.animate().rotation(0f).setDuration(200).start()
        } else {
            recyclerView.visibility = View.GONE
            // Flecha hacia abajo (ícono cerrado)
            button.animate().rotation(180f).setDuration(200).start()
        }
    }

    /**
     * Redirige al usuario al Hub de Bienvenida/Login tras el logout.
     */
    private fun navigateToLoginHub() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}