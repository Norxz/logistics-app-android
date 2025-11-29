package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.databinding.ActivityClientDashboardBinding
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.viewmodel.SolicitudViewModel
import co.edu.unipiloto.myapplication.viewmodel.SolicitudViewModelFactory

/**
 * 👨‍💻 Activity principal que actúa como el panel de control (Dashboard) para los usuarios con rol CLIENTE.
 * Es la VISTA en el patrón MVVM. Es responsable de:
 * 1. Inicializar la sesión y seguridad.
 * 2. Configurar la interfaz de usuario (RecyclerView, Listeners).
 * 3. Observar los datos ([SolicitudViewModel]) para actualizar la UI.
 * 4. Manejar las interacciones del usuario (navegación, botones de acción, toggles).
 */
class ClientDashboardActivity : AppCompatActivity() {

    // --- PROPIEDADES ---

    // Objeto generado por View Binding para acceder a las vistas del layout.
    private lateinit var binding: ActivityClientDashboardBinding

    // Objeto del ViewModel, encargado de la lógica de negocio y la gestión de datos.
    private lateinit var solicitudViewModel: SolicitudViewModel

    // Utilidad para gestionar el estado de la sesión (login, rol, ID del usuario).
    private lateinit var sessionManager: SessionManager

    // Adaptador para la lista de solicitudes ACTIVAS (pendientes, en curso).
    private lateinit var solicitadosAdapter: SolicitudAdapter

    // Adaptador para la lista de solicitudes FINALIZADAS (entregadas, canceladas).
    private lateinit var finalizadosAdapter: SolicitudAdapter

    // Estado UI: Visibilidad de la sección de solicitudes activas.
    private var isSolicitadosVisible = true

    // Estado UI: Visibilidad de la sección de solicitudes finalizadas.
    private var isFinalizadosVisible = false

    // --- CICLO DE VIDA ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar View Binding y establecer el contenido de la vista.
        binding = ActivityClientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        // 2. Seguridad y sesión crítica: Si no está logueado o no es CLIENTE, redirige.
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "CLIENTE") {
            Log.w("Dashboard", "Acceso no válido. Cerrando sesión.")
            sessionManager.logoutUser()
            navigateToLoginHub()
            return
        }

        // 3. Inicializar ViewModel (Configuración de dependencias).
        initViewModel()

        // 4. Configurar UI y Listeners.
        loadClientDataFromSession()
        setupAdapters()
        setupListeners()
        setupObservers()

        // 5. Establecer estado inicial de los toggles de visibilidad.
        updateToggleViews()
    }

    override fun onResume() {
        super.onResume()
        // 6. Cargar datos al iniciar o regresar a la Activity.
        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            // Carga las solicitudes del cliente al ViewModel.
            solicitudViewModel.loadSolicitudes(userId)
        }
    }

    // --- Inicialización y Configuración ---

    /**
     * Inicializa el [SolicitudViewModel] utilizando un [SolicitudViewModelFactory]
     * para inyectar la dependencia del repositorio y el API service.
     */
    private fun initViewModel() {
        val solicitudApi = RetrofitClient.getSolicitudApi()
        val repository = SolicitudRepository(solicitudApi)
        val factory = SolicitudViewModelFactory(repository)
        solicitudViewModel = ViewModelProvider(this, factory)[SolicitudViewModel::class.java]
    }

    /**
     * Carga el nombre del cliente desde [SessionManager] y actualiza el título de bienvenida.
     */
    private fun loadClientDataFromSession() {
        val clientName = sessionManager.getName().split(" ").firstOrNull() ?: "Cliente"
        binding.tvWelcomeTitle.text = getString(R.string.client_dashboard_title_welcome, clientName)
    }

    /**
     * Configura los dos [SolicitudAdapter] y sus respectivos [RecyclerView] (solicitados y finalizados).
     * Define la lambda [onActionClick] para manejar las acciones del usuario (Cancelar/Confirmar).
     */
    private fun setupAdapters() {
        // Adaptador para solicitudes activas
        solicitadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud: Solicitud, action: String -> handleSolicitudAction(solicitud, action) }
        )
        binding.rvSolicitados.adapter = solicitadosAdapter
        binding.rvSolicitados.layoutManager = LinearLayoutManager(this)

        // Adaptador para solicitudes finalizadas
        finalizadosAdapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole(),
            onActionClick = { solicitud: Solicitud, action: String -> handleSolicitudAction(solicitud, action) }
        )
        binding.rvFinalizados.adapter = finalizadosAdapter
        binding.rvFinalizados.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Configura los listeners de botones (Logout, Nueva Solicitud, Toggles de sección).
     */
    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            sessionManager.logoutUser()
            navigateToLoginHub()
        }

        binding.btnNuevaSolicitud.setOnClickListener {
            // Navegación a la Activity de creación de solicitudes
            startActivity(Intent(this, RecogidaActivity::class.java))
        }

        // Toggle de solicitudes activas
        binding.btnToggleSolicitados.setOnClickListener {
            isSolicitadosVisible = !isSolicitadosVisible
            toggleSection(binding.rvSolicitados, binding.tvEmptySolicitados, binding.btnToggleSolicitados, isSolicitadosVisible, solicitadosAdapter.itemCount)
        }

        // Toggle de solicitudes finalizadas
        binding.btnToggleFinalizados.setOnClickListener {
            isFinalizadosVisible = !isFinalizadosVisible
            toggleSection(binding.rvFinalizados, binding.tvEmptyFinalizados, binding.btnToggleFinalizados, isFinalizadosVisible, finalizadosAdapter.itemCount)
        }
    }

    /**
     * Inicializa la visibilidad de las secciones al inicio.
     */
    private fun updateToggleViews() {
        toggleSection(binding.rvSolicitados, binding.tvEmptySolicitados, binding.btnToggleSolicitados, isSolicitadosVisible, solicitadosAdapter.itemCount)
        toggleSection(binding.rvFinalizados, binding.tvEmptyFinalizados, binding.btnToggleFinalizados, isFinalizadosVisible, finalizadosAdapter.itemCount)
    }

    /**
     * Gestiona la visibilidad de una sección (RecyclerView/Vista vacía) y la rotación del botón toggle.
     *
     * @param recyclerView La vista RecyclerView que contiene los datos.
     * @param emptyView La vista TextView/Layout a mostrar si la lista está vacía.
     * @param button El botón toggle que controla la visibilidad.
     * @param newState El nuevo estado de visibilidad (true = visible).
     * @param itemCount Número de ítems en la lista.
     */
    private fun toggleSection(recyclerView: View, emptyView: View, button: View, newState: Boolean, itemCount: Int) {
        if (newState) {
            // Mostrar
            if (itemCount > 0) {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
            button.animate().rotation(0f).setDuration(200).start()
        } else {
            // Ocultar
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
            button.animate().rotation(180f).setDuration(200).start()
        }
    }

    // --- Observadores de Datos ---

    /**
     * Configura los observadores de [LiveData] desde el [SolicitudViewModel] para actualizar la UI.
     */
    private fun setupObservers() {
        // Observa solicitudes activas (pendientes, en curso)
        solicitudViewModel.activeSolicitudes.observe(this) { activas ->
            solicitadosAdapter.updateData(activas)
            toggleSection(binding.rvSolicitados, binding.tvEmptySolicitados, binding.btnToggleSolicitados, isSolicitadosVisible, activas.size)
        }

        // Observa solicitudes finalizadas (entregadas, canceladas)
        solicitudViewModel.finishedSolicitudes.observe(this) { finalizadas ->
            finalizadosAdapter.updateData(finalizadas)
            toggleSection(binding.rvFinalizados, binding.tvEmptyFinalizados, binding.btnToggleFinalizados, isFinalizadosVisible, finalizadas.size)
        }

        // Observa errores y los muestra en un Toast
        solicitudViewModel.error.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showToast(message)
                solicitudViewModel.clearError()
            }
        }

        // Observa el éxito de una acción (cancelar/confirmar)
        solicitudViewModel.actionSuccess.observe(this) { success ->
            if (!success.isNullOrEmpty()) {
                showToast(success)
                solicitudViewModel.clearActionSuccess()
                // Recarga la lista para reflejar el nuevo estado
                solicitudViewModel.loadSolicitudes(sessionManager.getUserId())
            }
        }
    }

    // --- Lógica de Acciones ---

    /**
     * Maneja la acción específica seleccionada por el usuario en un ítem de [Solicitud].
     *
     * @param solicitud El modelo de la solicitud sobre la que se realiza la acción.
     * @param action El String de la acción solicitada (ej: "CANCELAR_CLIENTE", "CONFIRMAR_ENTREGA").
     */
    private fun handleSolicitudAction(solicitud: Solicitud, action: String) {
        // Valida que el ID de la solicitud no sea nulo antes de continuar.
        val id = solicitud.id ?: run {
            showToast("Error de la solicitud: ID no disponible.")
            return
        }

        when (action) {
            "CANCELAR_CLIENTE" -> {
                showCancelConfirmationDialog(solicitud)
            }
            "CONFIRMAR_ENTREGA" -> {
                updateSolicitudState(id, "FINALIZADA")
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de enviar la acción de CANCELAR.
     */
    private fun showCancelConfirmationDialog(solicitud: Solicitud) {
        // Proporciona 0L para el mensaje si el ID fuera nulo (aunque ya fue validado antes).
        val idParaMensaje = solicitud.id ?: 0L

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_cancel))
            // Inserta el ID en el mensaje de confirmación.
            .setMessage(getString(R.string.dialog_message_cancel, idParaMensaje))
            .setPositiveButton(getString(R.string.dialog_positive_cancel)) { dialog, _ ->

                // Re-valida el ID para la llamada al ViewModel dentro del lambda.
                val idParaActualizar = solicitud.id ?: run {
                    showToast("Error al cancelar: ID no encontrado.")
                    return@setPositiveButton
                }
                updateSolicitudState(idParaActualizar, "CANCELADA")
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_negative_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Delega al [SolicitudViewModel] la tarea de actualizar el estado de una solicitud
     * en el backend.
     *
     * @param solicitudId El ID no nulo de la solicitud.
     * @param newState El nuevo estado (ej: "CANCELADA", "FINALIZADA").
     */
    private fun updateSolicitudState(solicitudId: Long, newState: String) {
        solicitudViewModel.updateSolicitudState(solicitudId, newState)
    }

    // --- Utilidades ---

    /**
     * Navega al hub de login ([MainActivity]) y limpia la pila de actividades.
     */
    private fun navigateToLoginHub() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Muestra un mensaje corto al usuario.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}