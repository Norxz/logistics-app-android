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
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.models.Solicitud
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

    private lateinit var solicitadosAdapter: SolicitudAdapter

    private lateinit var finalizadosAdapter: SolicitudAdapter

    // Mensaje de vacío
    private lateinit var tvEmpty: TextView

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    private lateinit var solicitudRepository: SolicitudRepository

    // Estado de la visibilidad
    private var isSolicitadosVisible = true
    private var isFinalizadosVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)
        solicitudRepository = SolicitudRepository(this)

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
        loadSolicitudes()
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
     * Obtiene el nombre del usuario directamente de SessionManager.
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
            val intent = Intent(this, RecogidaActivity::class.java).apply {
            }
            startActivity(intent)
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

    private fun setupAdapters() {
        // 1. Adaptador para Solicitudes Activas
        solicitadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud, action -> handleSolicitudAction(solicitud, action) }
        )
        rvSolicitados.adapter = solicitadosAdapter

        // 2. Adaptador para Historial Finalizado
        finalizadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud, action -> handleSolicitudAction(solicitud, action) }
        )
        rvFinalizados.adapter = finalizadosAdapter
    }

    /**
     * Carga las solicitudes del cliente logueado y las separa en Activas y Finalizadas.
     */
    private fun loadSolicitudes() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        // Usar un hilo diferente (o Coroutines en un proyecto real) para operaciones de base de datos
        Thread {
            try {
                // Obtener todas las solicitudes enriquecidas del usuario
                val allSolicitudes = solicitudRepository.getSolicitudesEnrichedByUserId(userId)

                // 1. Separar listas
                val activas = allSolicitudes.filter { isSolicitudActiva(it.estado) }
                val finalizadas = allSolicitudes.filter { !isSolicitudActiva(it.estado) }

                // 2. Actualizar la UI en el hilo principal
                runOnUiThread {
                    solicitadosAdapter.updateData(activas)
                    finalizadosAdapter.updateData(finalizadas)

                    val totalSolicitudes = activas.size + finalizadas.size

                    if (totalSolicitudes == 0) {
                        // Si no hay ninguna solicitud, muestra el mensaje de vacío y oculta las listas.
                        tvEmpty.visibility = View.VISIBLE
                        rvSolicitados.visibility = View.GONE
                        rvFinalizados.visibility = View.GONE
                        // También puedes ocultar los headers si quieres un dashboard más limpio.
                        tvHeaderSolicitados.visibility = View.GONE
                        tvHeaderFinalizados.visibility = View.GONE
                    } else {
                        // Si hay solicitudes, oculta el mensaje de vacío y restablece la visibilidad de las listas según el estado de toggle
                        tvEmpty.visibility = View.GONE

                        // Restablece la visibilidad de los encabezados (si se ocultaron)
                        tvHeaderSolicitados.visibility = View.VISIBLE
                        tvHeaderFinalizados.visibility = View.VISIBLE

                        // Solo muestra las RecyclerViews si tienen datos Y si su toggle está activo.
                        rvSolicitados.visibility = if (activas.isNotEmpty() && isSolicitadosVisible) View.VISIBLE else View.GONE
                        rvFinalizados.visibility = if (finalizadas.isNotEmpty() && isFinalizadosVisible) View.VISIBLE else View.GONE

                        // Manejar el caso donde una lista está vacía pero la otra no
                        if (activas.isEmpty()) {
                            // Puedes agregar un TextView "Sin solicitudes activas" aquí si fuera necesario.
                            Log.d("Dashboard", "No hay solicitudes activas, ocultando rvSolicitados.")
                        }
                        if (finalizadas.isEmpty()) {
                            Log.d("Dashboard", "No hay historial, ocultando rvFinalizados.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Dashboard", "Error al cargar solicitudes: ${e.message}")
            }
        }.start()
    }

    /**
     * Lógica para determinar si una solicitud está activa (no finalizada ni cancelada).
     */
    private fun isSolicitudActiva(estado: String): Boolean {
        val estadoUpper = estado.uppercase()
        return estadoUpper !in listOf("ENTREGADA", "FINALIZADA", "CANCELADA")
    }

    /**
     * Maneja el clic en los botones del item_solicitud (e.g., Cancelar, Confirmar Entrega).
     */
    private fun handleSolicitudAction(solicitud: Solicitud, action: String) {
        Log.d("Dashboard", "Acción: $action en Solicitud ID: ${solicitud.id}")

        // Las acciones de los clientes generalmente son CANCELAR y CONFIRMAR_ENTREGA.
        when (action) {
            "CANCELAR_CLIENTE" -> {
                showCancelConfirmationDialog(solicitud)
            }
            "CONFIRMAR_ENTREGA" -> {
                // Lógica para CONFIRMAR la entrega (cambia el estado a "FINALIZADA" o similar)
                // Llama al repositorio para actualizar el estado.
            }
        }
        // Después de una acción exitosa, se debe volver a llamar a loadSolicitudes()
        // para refrescar las listas.
        // loadSolicitudes()
    }

    private fun showCancelConfirmationDialog(solicitud: Solicitud) {
        androidx.appcompat.app.AlertDialog.Builder(this)
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
     * Actualiza el estado de la solicitud en la DB y refresca las listas.
     */
    private fun updateSolicitudState(solicitudId: Long, newState: String) {
        Thread {
            try {
                // Asegúrate de que SolicitudRepository tenga el método actualizarEstado.
                // Si el método pide recolectorId, pasamos null ya que el cliente lo está haciendo.
                val rowsAffected = solicitudRepository.actualizarEstado(solicitudId, newState)

                runOnUiThread {
                    if (rowsAffected > 0) {
                        // Si la actualización fue exitosa, recargamos la data para que se muevan las listas
                        loadSolicitudes()
                        showToast("Solicitud #${solicitudId} ha sido marcada como $newState.")
                    } else {
                        showToast("Error: No se pudo actualizar el estado de la solicitud.")
                    }
                }
            } catch (e: Exception) {
                Log.e("Dashboard", "Error al actualizar estado: ${e.message}")
                runOnUiThread {
                    showToast("Error de base de datos al cancelar.")
                }
            }
        }.start()
    }

    /**
     * Función helper para mostrar mensajes.
     */
    private fun showToast(message: String) {
        runOnUiThread {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

}