package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.ui.SolicitudAdapter

/**
 * Activity principal del cliente (Dashboard), mapeada al layout activity_user_dashboard.xml.
 * Muestra solicitudes activas y finalizadas.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var repo: SolicitudRepository
    private lateinit var rvSolicitados: RecyclerView
    private lateinit var rvFinalizados: RecyclerView
    private lateinit var btnToggleSolicitados: ImageButton
    private lateinit var btnToggleFinalizados: ImageButton
    private lateinit var btnNuevaSolicitud: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard) // Usamos activity_user_dashboard.xml

        session = SessionManager(this)
        repo = SolicitudRepository(this)

        if (!session.isLoggedIn() || session.getRole() != "CLIENTE") {
            // Si el usuario no es cliente o no está logueado, lo enviamos al inicio
            session.logoutUser()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        initViews()
        setupListeners()
        setupRecyclerViews()
        cargarLista()
    }

    private fun initViews() {
        rvSolicitados = findViewById(R.id.rvSolicitados)
        rvFinalizados = findViewById(R.id.rvFinalizados)
        btnToggleSolicitados = findViewById(R.id.btnToggleSolicitados)
        btnToggleFinalizados = findViewById(R.id.btnToggleFinalizados)
        btnNuevaSolicitud = findViewById(R.id.btnNuevaSolicitud)
        btnLogout = findViewById(R.id.btnLogout)
        tvEmpty = findViewById(R.id.tvEmpty)
        // Puedes personalizar tvWelcomeTitle si quieres usar el nombre del cliente
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            session.logoutUser()
            // Vuelve a la pantalla de bienvenida
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        btnNuevaSolicitud.setOnClickListener {
            // Inicia el flujo de creación de solicitud
            startActivity(Intent(this, PickUpLocationActivity::class.java))
        }

        btnToggleSolicitados.setOnClickListener {
            toggleVisibility(rvSolicitados, btnToggleSolicitados, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down)
        }
        btnToggleFinalizados.setOnClickListener {
            toggleVisibility(rvFinalizados, btnToggleFinalizados, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down)
        }
    }

    private fun setupRecyclerViews() {
        rvSolicitados.layoutManager = LinearLayoutManager(this)
        rvFinalizados.layoutManager = LinearLayoutManager(this)
        // Adaptadores se asignan en cargarLista()
    }

    override fun onResume() {
        super.onResume()
        cargarLista()
    }

    private fun toggleVisibility(rv: RecyclerView, btn: ImageButton, iconUp: Int, iconDown: Int) {
        if (rv.visibility == View.VISIBLE) {
            rv.visibility = View.GONE
            btn.setImageResource(iconDown)
        } else {
            rv.visibility = View.VISIBLE
            btn.setImageResource(iconUp)
        }
    }

    private fun cargarLista() {
        val userId = session.getUserId()
        // Asegurarse de que el icono ic_arrow_up y ic_arrow_down existan en tus drawables

        val allItems = repo.listarPorUsuario(userId) ?: emptyList()

        val itemsActivas = mutableListOf<SolicitudRepository.SolicitudItem>()
        val itemsFinalizadas = mutableListOf<SolicitudRepository.SolicitudItem>()

        for (item in allItems) {
            if (item.estado.equals("ENTREGADA", ignoreCase = true) || item.estado.equals("CANCELADA", ignoreCase = true)) {
                itemsFinalizadas.add(item)
            } else {
                itemsActivas.add(item)
            }
        }

        val adapterSolicitados = SolicitudAdapter.forCliente(itemsActivas)
        rvSolicitados.adapter = adapterSolicitados

        adapterSolicitados.setOnCancelListener { solicitudId, _ ->
            val rows = repo.cancelarSolicitud(solicitudId, session.getUserId())
            if (rows > 0) {
                Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                cargarLista() // Recarga la lista
            } else {
                Toast.makeText(this, "No se pudo cancelar (estado no es PENDIENTE).", Toast.LENGTH_SHORT).show()
                cargarLista()
            }
        }

        rvFinalizados.adapter = SolicitudAdapter.forCliente(itemsFinalizadas)

        // Manejo del estado vacío y visibilidad
        if (itemsActivas.isEmpty() && itemsFinalizadas.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        // Colapsar secciones vacías y configurar toggles
        if (itemsActivas.isEmpty()) {
            rvSolicitados.visibility = View.GONE
            btnToggleSolicitados.setImageResource(R.drawable.ic_arrow_down)
        } else {
            // Si tiene items activos, se asegura de que esté visible (estado por defecto)
            if (rvSolicitados.visibility == View.GONE) rvSolicitados.visibility = View.VISIBLE
            btnToggleSolicitados.setImageResource(R.drawable.ic_arrow_up)
        }

        if (itemsFinalizadas.isEmpty()) {
            rvFinalizados.visibility = View.GONE
            btnToggleFinalizados.setImageResource(R.drawable.ic_arrow_down)
        } else {
            // Por defecto, historial suele estar colapsado
            if (rvFinalizados.visibility == View.VISIBLE) {
                btnToggleFinalizados.setImageResource(R.drawable.ic_arrow_up)
            } else {
                btnToggleFinalizados.setImageResource(R.drawable.ic_arrow_down)
            }
        }
    }
}