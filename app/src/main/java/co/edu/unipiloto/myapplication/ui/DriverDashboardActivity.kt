package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.db.UserRepository
import com.google.android.material.button.MaterialButton

/**
 * Activity para el panel de control (dashboard) del conductor.
 *
 * Esta pantalla muestra una lista de las rutas o solicitudes de recolección que han sido
 * asignadas al conductor que ha iniciado sesión. También proporciona una opción para
 * cerrar la sesión.
 *
 * La actividad realiza las siguientes funciones clave:
 * 1. Verifica la sesión del usuario para asegurarse de que es un "CONDUCTOR".
 * 2. Obtiene los datos del conductor (ID y zona) de [SessionManager].
 * 3. Muestra un saludo personalizado con el nombre y la zona del conductor.
 * 4. Carga y muestra las solicitudes asignadas desde [SolicitudRepository] en un [RecyclerView].
 * 5. Muestra un mensaje si no hay rutas asignadas.
 * 6. Permite al usuario cerrar la sesión.
 */
class DriverDashboardActivity : AppCompatActivity() {

    // --- VISTAS ---
    /** TextView para mostrar el título principal, personalizado con el nombre del conductor. */
    private lateinit var tvDriverTitle: TextView
    /** TextView para mostrar un subtítulo, generalmente con la zona del conductor. */
    private lateinit var tvDriverSubtitle: TextView
    /** Botón para que el conductor cierre su sesión. */
    private lateinit var btnLogout: MaterialButton
    /** RecyclerView para mostrar la lista de rutas/solicitudes asignadas. */
    private lateinit var recyclerViewRoutes: RecyclerView
    /** TextView que se muestra cuando no hay rutas asignadas. */
    private lateinit var tvNoRoutes: TextView

    // --- UTILIDADES ---
    /** Gestor de sesión para obtener datos del usuario y manejar el estado de login. */
    private lateinit var sessionManager: SessionManager
    /** Repositorio para obtener información de los usuarios, como el nombre completo. */
    private lateinit var userRepository: UserRepository
    /** Repositorio para obtener las solicitudes de recolección. */
    private lateinit var solicitudRepository: SolicitudRepository
    /** Adaptador para el RecyclerView que muestra las solicitudes. */
    private lateinit var adapter: SolicitudAdapter

    // --- DATOS DE SESIÓN ---
    /** ID del conductor logueado, obtenido de SessionManager. */
    private var driverId: Long = -1L
    /** Zona del conductor logueado, obtenida de SessionManager. */
    private var driverZona: String? = null

    /**
     * Método principal que se llama al crear la actividad.
     *
     * Se encarga de la configuración inicial, incluyendo la verificación de la sesión,
     * la inicialización de las vistas y la carga de datos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        // Inicializar gestores y repositorios
        sessionManager = SessionManager(this)
        solicitudRepository = SolicitudRepository(this)
        userRepository = UserRepository(this)

        // Verificar si el usuario está logueado y tiene el rol correcto.
        // Si no, se cierra la sesión y se le redirige al login.
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
        loadAssignedRoutes()
    }

    /**
     * Inicializa y mapea las vistas del layout a las propiedades de la clase.
     * También personaliza los textos de bienvenida con los datos del conductor.
     */
    private fun initViews() {
        tvDriverTitle = findViewById(R.id.tvDriverTitle)
        tvDriverSubtitle = findViewById(R.id.tvDriverSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)

        // Obtiene el nombre del conductor desde la base de datos usando su ID.
        val driverName = userRepository.getFullNameById(driverId)

        // Personaliza el título con el nombre real o "Conductor" si no se encuentra.
        tvDriverTitle.text = getString(R.string.driver_dashboard_title, driverName ?: "Conductor")

        // Muestra la zona asignada o un texto por defecto.
        tvDriverSubtitle.text =
            getString(R.string.driver_dashboard_subtitle, driverZona ?: "Sin Zona")
    }

    /**
     * Configura los listeners para los elementos interactivos de la UI, como botones.
     */
    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Configura el RecyclerView, incluyendo su LayoutManager y su adaptador.
     * Utiliza un método de fábrica `forConductor` para instanciar el adaptador,
     * lo que permite una configuración específica para la vista del conductor.
     */
    private fun setupRecyclerView() {
        // Se asume la existencia de un método de fábrica `forConductor` en SolicitudAdapter.
        adapter = SolicitudAdapter.forConductor(items = emptyList())

        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.adapter = adapter
    }

    /**
     * Carga las rutas (solicitudes) asignadas al conductor desde el repositorio.
     *
     * Actualiza el adaptador del RecyclerView con los datos obtenidos. Si no hay
     * solicitudes, muestra un mensaje indicándolo y oculta la lista.
     */
    private fun loadAssignedRoutes() {
        if (driverId == -1L) {
            Toast.makeText(this, "Error: ID de conductor no válido.", Toast.LENGTH_LONG).show()
            return
        }

        // Se asume la existencia de un método en SolicitudRepository para obtener solicitudes por ID de recolector.
        val assignedSolicitudes = solicitudRepository.getSolicitudesByRecolectorId(driverId)

        if (assignedSolicitudes.isNotEmpty()) {
            adapter.updateData(assignedSolicitudes)
            recyclerViewRoutes.visibility = View.VISIBLE
            tvNoRoutes.visibility = View.GONE
        } else {
            recyclerViewRoutes.visibility = View.GONE
            tvNoRoutes.visibility = View.VISIBLE
        }
    }

    /**
     * Cierra la sesión del usuario actual, limpia los datos de SessionManager
     * y redirige a la pantalla de LoginActivity.
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        // Las flags borran el historial de actividades para que el usuario no pueda volver atrás.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}