package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity que actúa como el panel principal para los usuarios con rol CLIENTE.
 * Muestra las solicitudes activas e historial.
 */
class ClientDashboardActivity : AppCompatActivity() {

    // --- VISTAS ---
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnNuevaSolicitud: MaterialButton
    private lateinit var tvHeaderSolicitados: TextView
    private lateinit var btnToggleSolicitados: View
    private lateinit var rvSolicitados: RecyclerView
    private lateinit var tvHeaderFinalizados: TextView
    private lateinit var btnToggleFinalizados: View
    private lateinit var rvFinalizados: RecyclerView
    private lateinit var solicitadosAdapter: SolicitudAdapter
    private lateinit var finalizadosAdapter: SolicitudAdapter
    private lateinit var tvEmpty: TextView

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager

    // Estado de la visibilidad
    private var isSolicitadosVisible = true
    private var isFinalizadosVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)


        // 1. Verificar sesión crítica
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "CLIENTE") {
            Log.w("Dashboard", "Attempted access without valid CLIENTE session. Logging out.")
            sessionManager.logoutUser()
            navigateToLoginHub()
            return
        }

        initViews()
        loadClientDataFromSession()
        setupListeners()
        setupAdapters()
        // La carga se realiza en onResume
    }


    override fun onResume() {
        super.onResume()
        // Carga y refresca las solicitudes cada vez que se vuelve a la actividad.
        loadSolicitudes()
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

        rvSolicitados.layoutManager = LinearLayoutManager(this)
        rvFinalizados.layoutManager = LinearLayoutManager(this)

        rvSolicitados.visibility = if (isSolicitadosVisible) View.VISIBLE else View.GONE
        rvFinalizados.visibility = if (isFinalizadosVisible) View.VISIBLE else View.GONE

        btnToggleSolicitados.rotation = if (isSolicitadosVisible) 0f else 180f
        btnToggleFinalizados.rotation = if (isFinalizadosVisible) 0f else 180f
    }

    private fun loadClientDataFromSession() {
        val clientName = sessionManager.getName().split(" ").firstOrNull() ?: "Cliente"
        tvWelcomeTitle.text = getString(R.string.client_dashboard_title_welcome, clientName)
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            sessionManager.logoutUser()
            navigateToLoginHub()
        }

        btnNuevaSolicitud.setOnClickListener {
            startActivity(Intent(this, RecogidaActivity::class.java))
        }

        btnToggleSolicitados.setOnClickListener {
            isSolicitadosVisible = !isSolicitadosVisible
            toggleSection(rvSolicitados, btnToggleSolicitados, isSolicitadosVisible)
        }

        btnToggleFinalizados.setOnClickListener {
            isFinalizadosVisible = !isFinalizadosVisible
            toggleSection(rvFinalizados, btnToggleFinalizados, isFinalizadosVisible)
        }
    }

    private fun toggleSection(recyclerView: RecyclerView, button: View, newState: Boolean) {
        if (newState) {
            recyclerView.visibility = View.VISIBLE
            button.animate().rotation(0f).setDuration(200).start()
        } else {
            recyclerView.visibility = View.GONE
            button.animate().rotation(180f).setDuration(200).start()
        }
    }

    private fun navigateToLoginHub() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupAdapters() {
        solicitadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud, action -> handleSolicitudAction(solicitud, action) }
        )
        rvSolicitados.adapter = solicitadosAdapter

        finalizadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud, action -> handleSolicitudAction(solicitud, action) }
        )
        rvFinalizados.adapter = finalizadosAdapter
    }

    /**
     * Carga las solicitudes del cliente logueado usando el servicio REST.
     */
    private fun loadSolicitudes() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        // 🏆 LLAMADA A RETROFIT
        RetrofitClient.apiService.getSolicitudesByClient(userId).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                if (response.isSuccessful && response.body() != null) {
                    val allSolicitudes = response.body()!!

                    // 1. Separar listas
                    val activas = allSolicitudes.filter { isSolicitudActiva(it.estado) }
                    val finalizadas = allSolicitudes.filter { !isSolicitudActiva(it.estado) }

                    runOnUiThread {
                        solicitadosAdapter.updateData(activas)
                        finalizadosAdapter.updateData(finalizadas)

                        // 2. Lógica de visibilidad
                        val totalSolicitudes = activas.size + finalizadas.size

                        if (totalSolicitudes == 0) {
                            tvEmpty.visibility = View.VISIBLE
                            rvSolicitados.visibility = View.GONE
                            rvFinalizados.visibility = View.GONE
                            tvHeaderSolicitados.visibility = View.GONE
                            tvHeaderFinalizados.visibility = View.GONE
                        } else {
                            tvEmpty.visibility = View.GONE
                            tvHeaderSolicitados.visibility = View.VISIBLE
                            tvHeaderFinalizados.visibility = View.VISIBLE

                            // Mostrar solo si hay datos Y el toggle lo permite
                            rvSolicitados.visibility = if (activas.isNotEmpty() && isSolicitadosVisible) View.VISIBLE else View.GONE
                            rvFinalizados.visibility = if (finalizadas.isNotEmpty() && isFinalizadosVisible) View.VISIBLE else View.GONE
                        }
                    }
                } else {
                    Log.e("Dashboard", "Error ${response.code()} al cargar solicitudes: ${response.message()}")
                    Toast.makeText(this@ClientDashboardActivity, "Error al cargar solicitudes.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("Dashboard", "Fallo de red: ${t.message}")
                Toast.makeText(this@ClientDashboardActivity, "Fallo de red al conectarse al servidor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun isSolicitudActiva(estado: String): Boolean {
        val estadoUpper = estado.uppercase()
        return estadoUpper !in listOf("ENTREGADA", "FINALIZADA", "CANCELADA")
    }

    private fun handleSolicitudAction(solicitud: Solicitud, action: String) {
        when (action) {
            "CANCELAR_CLIENTE" -> {
                showCancelConfirmationDialog(solicitud)
            }
            "CONFIRMAR_ENTREGA" -> {
                // Implementación de confirmación de entrega
                updateSolicitudState(solicitud.id, "FINALIZADA")
            }
        }
    }

    private fun showCancelConfirmationDialog(solicitud: Solicitud) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Solicitud")
            .setMessage("¿Estás seguro de que deseas cancelar la solicitud #${solicitud.id}? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, Cancelar") { dialog, _ ->
                updateSolicitudState(solicitud.id, "CANCELADA")
                dialog.dismiss()
            }
            .setNegativeButton("No, Mantener") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Actualiza el estado de la solicitud en el Backend (PUT /api/v1/solicitudes/{id}/estado).
     */
    private fun updateSolicitudState(solicitudId: Long, newState: String) {
        val requestBody = mapOf("estado" to newState) // Cuerpo JSON para el backend

        // 🏆 LLAMADA A RETROFIT PARA ACTUALIZAR ESTADO
        RetrofitClient.apiService.actualizarEstado(solicitudId, requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    loadSolicitudes() // Refresca las listas (la solicitud se moverá a "Finalizados")
                    showToast("Solicitud #${solicitudId} ha sido marcada como $newState.")
                } else {
                    Log.e("Dashboard", "Error ${response.code()} al actualizar estado: ${response.message()}")
                    showToast("Error: No se pudo actualizar el estado de la solicitud.")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Error de red al actualizar estado.")
            }
        })
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}