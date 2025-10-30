package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * Actividad principal (Dashboard del Cliente) en Kotlin.
 * Muestra la lista de solicitudes y maneja la redirección de roles.
 */
class MainActivity : AppCompatActivity() {

    // Se recomienda usar View Binding para acceder a las vistas
    // private lateinit var binding: ActivityMainBinding

    private lateinit var session: SessionManager
    private lateinit var repo: SolicitudRepository
    private var adapterSolicitados: SolicitudAdapter? = null
    private var adapterFinalizados: SolicitudAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de SessionManager
        session = SessionManager(this)

        // 1. Redirección Crítica: Verificar la sesión
        val userId = session.getUserId()
        if (userId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. Redirección de Roles: Si el usuario es Logístico, enviarlo a su Dashboard.
        val role = session.getRole()
        val upperRole = role.trim().uppercase()
        // Comprobación de todos los roles logísticos
        if (upperRole == "CONDUCTOR" || upperRole == "GESTOR" ||
            upperRole == "FUNCIONARIO" || upperRole == "ANALISTA") {

            val dest = getDestinationIntentByRole(upperRole)
            startActivity(dest)
            finish()
            return
        }
        // Si no se redirige, continuamos como Cliente (MainActivity).

        // ------ Panel Cliente (Lógica de la interfaz) ------
        try {
            repo = SolicitudRepository(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar SolicitudRepository: ${e.message}")
            Toast.makeText(this, "Error de base de datos. Intente de nuevo.", Toast.LENGTH_LONG).show()
            // Si el repo falla al inicio, es mejor no continuar
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 3. Inicialización de Vistas y Layouts
        val rvSolicitados: RecyclerView = findViewById(R.id.rvSolicitados)
        rvSolicitados.layoutManager = LinearLayoutManager(this)
        val rvFinalizados: RecyclerView = findViewById(R.id.rvFinalizados)
        rvFinalizados.layoutManager = LinearLayoutManager(this)

        // Botones y Listeners
        findViewById<View>(R.id.btnNuevaSolicitud).setOnClickListener {
            startActivity(Intent(this, SolicitudActivity::class.java))
        }

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Lógica de Toggle (Desplegar/Colapsar)
        findViewById<ImageButton>(R.id.btnToggleSolicitados).setOnClickListener {
            toggleVisibility(rvSolicitados, findViewById(R.id.btnToggleSolicitados))
        }
        findViewById<ImageButton>(R.id.btnToggleFinalizados).setOnClickListener {
            toggleVisibility(rvFinalizados, findViewById(R.id.btnToggleFinalizados))
        }

        cargarLista()
    }

    override fun onResume() {
        super.onResume()
        // Asegurarse de que el repositorio se haya inicializado antes de cargar la lista
        if (::repo.isInitialized) {
            cargarLista()
        }
    }

    /**
     * Alterna la visibilidad de un RecyclerView y actualiza el ícono del botón.
     */
    private fun toggleVisibility(rv: RecyclerView, btn: ImageButton) {
        if (rv.visibility == View.VISIBLE) {
            rv.visibility = View.GONE
            btn.setImageResource(R.drawable.ic_arrow_down) // Asume que tienes este recurso
        } else {
            rv.visibility = View.VISIBLE
            btn.setImageResource(R.drawable.ic_arrow_up) // Asume que tienes este recurso
        }
    }

    /**
     * Carga y separa la lista de solicitudes del usuario actual en Activas y Finalizadas.
     */
    private fun cargarLista() {
        val userId = session.getUserId()
        val allItems = repo.listarPorUsuario(userId) ?: emptyList()

        val itemsActivas = mutableListOf<SolicitudRepository.SolicitudItem>()
        val itemsFinalizadas = mutableListOf<SolicitudRepository.SolicitudItem>()

        for (item in allItems) {
            // Lógica de filtrado con .equals(ignoreCase = true) de Kotlin
            if (item.estado.equals("ENTREGADA", ignoreCase = true) || item.estado.equals("CANCELADA", ignoreCase = true)) {
                itemsFinalizadas.add(item)
            } else {
                itemsActivas.add(item)
            }
        }

        // 1. CONFIGURACIÓN DE LISTA ACTIVA
        adapterSolicitados = SolicitudAdapter.forCliente(itemsActivas)
        findViewById<RecyclerView>(R.id.rvSolicitados).adapter = adapterSolicitados

        adapterSolicitados?.setOnCancelListener { solicitudId, _ ->
            val rows = repo.cancelarSolicitud(solicitudId, session.getUserId())
            if (rows > 0) {
                Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                cargarLista()
            } else {
                Toast.makeText(this, "No se pudo cancelar (quizá ya fue aceptada)", Toast.LENGTH_SHORT).show()
                cargarLista()
            }
        }

        // 2. CONFIGURACIÓN DE LISTA FINALIZADA
        adapterFinalizados = SolicitudAdapter.forCliente(itemsFinalizadas)
        findViewById<RecyclerView>(R.id.rvFinalizados).adapter = adapterFinalizados

        // 3. MANEJO DEL ESTADO VACÍO Y VISIBILIDAD
        val tvEmpty = findViewById<View>(R.id.tvEmpty)
        if (itemsActivas.isEmpty() && itemsFinalizadas.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        // Colapsar secciones vacías
        if (itemsActivas.isEmpty()) {
            findViewById<RecyclerView>(R.id.rvSolicitados).visibility = View.GONE
            findViewById<ImageButton>(R.id.btnToggleSolicitados).setImageResource(R.drawable.ic_arrow_down)
        } else {
            findViewById<RecyclerView>(R.id.rvSolicitados).visibility = View.VISIBLE
        }

        if (itemsFinalizadas.isEmpty()) {
            findViewById<RecyclerView>(R.id.rvFinalizados).visibility = View.GONE
            findViewById<ImageButton>(R.id.btnToggleFinalizados).setImageResource(R.drawable.ic_arrow_down)
        }
    }

    /**
     * Obtiene el Intent de destino basándose en el rol (Consistente con LoginActivity).
     */
    private fun getDestinationIntentByRole(role: String): Intent {
        val destClass = when (role.trim().toUpperCase()) {
            "CONDUCTOR" -> DriverDashboardActivity::class.java
            "GESTOR" -> ManagerDashboardActivity::class.java
            "FUNCIONARIO" -> BranchDashboardActivity::class.java
            "ANALISTA" -> AdminPanelActivity::class.java
            "CLIENTE" -> MainActivity::class.java
            else -> MainActivity::class.java
        }
        return Intent(this, destClass)
    }
}