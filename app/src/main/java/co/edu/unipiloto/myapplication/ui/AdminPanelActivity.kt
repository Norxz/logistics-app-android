package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton

/**
 * Activity para el Panel de Administración.
 * Muestra estadísticas en tiempo real y accesos directos a la gestión.
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var solicitudApi: SolicitudApi

    // Vistas: Botones
    private lateinit var tvAdminTitle: TextView
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnManageBranches: MaterialButton
    private lateinit var btnViewAllRequests: MaterialButton
    private lateinit var btnLogoutAdmin: MaterialButton

    // Vistas: Contadores de Estadísticas (6 Estados)
    private lateinit var tvCountAssigned: TextView
    private lateinit var tvCountCollection: TextView
    private lateinit var tvCountDistribution: TextView
    private lateinit var tvCountReparto: TextView
    private lateinit var tvCountDelivered: TextView
    private lateinit var tvCountCancelled: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        supportActionBar?.hide()

        // Inicializar Servicios
        sessionManager = SessionManager(this)
        solicitudApi = RetrofitClient.getSolicitudApi()

        // Verificación de seguridad: Rol ADMIN
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "ADMIN") {
            logoutUser()
            return
        }

        initViews()
        setupListeners()
        loadDashboardStatistics()
    }

    // Recargar estadísticas al volver de otra pantalla (ej. después de editar un usuario)
    override fun onResume() {
        super.onResume()
        loadDashboardStatistics()
    }

    private fun initViews() {
        tvAdminTitle = findViewById(R.id.tvAdminTitle)

        // Botones de Navegación
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnManageBranches = findViewById(R.id.btnManageBranches)
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)

        // Contadores del Dashboard (Deben coincidir con los IDs del XML nuevo)
        tvCountAssigned = findViewById(R.id.tvCountAssigned)
        tvCountCollection = findViewById(R.id.tvCountCollection)
        tvCountDistribution = findViewById(R.id.tvCountDistribution)
        tvCountReparto = findViewById(R.id.tvCountReparto)
        tvCountDelivered = findViewById(R.id.tvCountDelivered)
        tvCountCancelled = findViewById(R.id.tvCountCancelled)
    }

    private fun setupListeners() {
        // 1. Gestión de Usuarios
        btnManageUsers.setOnClickListener {
            startActivity(Intent(this, LogisticUserManagementActivity::class.java))
        }

        // 2. Gestión de Sucursales
        btnManageBranches.setOnClickListener {
            startActivity(Intent(this, ManageBranchesActivity::class.java))
        }

        // 3. Ver todas las solicitudes
        btnViewAllRequests.setOnClickListener {
            startActivity(Intent(this, ViewAllRequestsActivity::class.java))
        }

        // 4. Logout
        btnLogoutAdmin.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Carga todas las solicitudes y calcula los totales por estado.
     */
    private fun loadDashboardStatistics() {
        lifecycleScope.launch {
            try {
                // Llamamos a la API (Endpoint que devuelve la lista completa)
                val response = solicitudApi.getAllSolicitudes()

                if (response.isSuccessful) {
                    val allRequests = response.body() ?: emptyList()

                    // --- CÁLCULO DE ESTADÍSTICAS ---
                    // Contamos cuántas hay en cada estado clave
                    val assigned = allRequests.count { it.estado == "ASIGNADA" }
                    val collection = allRequests.count { it.estado == "EN_RUTA_RECOLECCION" }
                    val distribution = allRequests.count { it.estado == "EN_DISTRIBUCION" } // En Bodega
                    val reparto = allRequests.count { it.estado == "EN_RUTA_REPARTO" }      // En camino a entrega
                    val delivered = allRequests.count { it.estado == "ENTREGADA" }
                    val cancelled = allRequests.count { it.estado == "CANCELADA" }

                    // --- ACTUALIZAR LA INTERFAZ ---
                    tvCountAssigned.text = assigned.toString()
                    tvCountCollection.text = collection.toString()
                    tvCountDistribution.text = distribution.toString()
                    tvCountReparto.text = reparto.toString()
                    tvCountDelivered.text = delivered.toString()
                    tvCountCancelled.text = cancelled.toString()

                } else {
                    Toast.makeText(this@AdminPanelActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    setUnknownStatistics()
                }
            } catch (e: Exception) {
                // Si hay error de red, ponemos guiones
                setUnknownStatistics()
                // Opcional: Toast.makeText(this@AdminPanelActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Helper para poner guiones si falla la carga
     */
    private fun setUnknownStatistics() {
        val dash = "-"
        tvCountAssigned.text = dash
        tvCountCollection.text = dash
        tvCountDistribution.text = dash
        tvCountReparto.text = dash
        tvCountDelivered.text = dash
        tvCountCancelled.text = dash
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}