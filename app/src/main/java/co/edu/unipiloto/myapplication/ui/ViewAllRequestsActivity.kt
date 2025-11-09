package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R // Importación de R corregida
import co.edu.unipiloto.myapplication.adapters.RequestAdapter // Asumiendo este path para el adaptador
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.Request // Importación del modelo de datos correcto
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import kotlin.concurrent.thread // Necesario para ejecutar operaciones de BD fuera del hilo principal

/**
 * Activity para el administrador: Muestra todas las solicitudes del sistema.
 */
class ViewAllRequestsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    private lateinit var btnBack: ImageButton
    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var btnLogoutRequests: MaterialButton
    private lateinit var adapter: RequestAdapter
    private var requestList: MutableList<Request> = mutableListOf() // Inicializar como mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_requests)

        supportActionBar?.hide()

        // Inicialización de la lógica
        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)

        // 1. Verificar sesión de administrador
        if (sessionManager.getRole() != "ADMIN") {
            logoutUser()
            return
        }

        initViews()
        setupListeners()
        setupRecyclerView()

        // 2. Cargar datos desde la base de datos
        loadRequests()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        recyclerViewRequests = findViewById(R.id.recyclerViewRequests)
        btnLogoutRequests = findViewById(R.id.btnLogoutRequests)
    }

    private fun setupListeners() {
        // Botón de regreso
        btnBack.setOnClickListener {
            // Regresar al Panel de Administración
            finish()
        }

        // Botón de cerrar sesión
        btnLogoutRequests.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupRecyclerView() {
        recyclerViewRequests.layoutManager = LinearLayoutManager(this)
        recyclerViewRequests.setHasFixedSize(true)

        // Inicializamos el adaptador con la lista vacía/mutable
        adapter = RequestAdapter(requestList) { request ->
            // Manejar click en el botón GESTIONAR
            handleManageRequestClick(request)
        }
        recyclerViewRequests.adapter = adapter
    }

    private fun loadRequests() {
        // Las operaciones de base de datos DEBEN ejecutarse en un hilo secundario
        thread {
            val fetchedRequests = userRepository.getAllRequests()

            // Volver al hilo principal para actualizar la UI (RecyclerView)
            runOnUiThread {
                if (fetchedRequests.isNotEmpty()) {
                    requestList.clear()
                    requestList.addAll(fetchedRequests)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No hay solicitudes pendientes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleManageRequestClick(request: Request) {
        // Aquí defines la lógica de gestión (abrir un diálogo o una nueva Activity)
        Toast.makeText(this, "Gestionando Guía: ${request.guiaId}", Toast.LENGTH_SHORT).show()

        // EJEMPLO: Abrir una nueva Activity para editar/asignar la solicitud
        // val intent = Intent(this, RequestDetailActivity::class.java)
        // intent.putExtra("REQUEST_ID", request.id)
        // startActivity(intent)
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}