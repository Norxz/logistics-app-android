package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Activity para el Dashboard del Funcionario de Sucursal.
 * Muestra listas colapsables de solicitudes PENDIENTES, EN RUTA e HISTORIAL.
 */
class BranchDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_functionary_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "FUNCIONARIO") {
            logoutUser()
            return
        }

        // Simplemente usaremos los RecyclerViews directamente en la Activity (simplificado)
        // aunque tu XML sugiere que estos podrían ser Fragments.
        setupRecyclerAndToggles()
        setupListeners()
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            logoutUser()
        }

        findViewById<MaterialButton>(R.id.btnNewRequest).setOnClickListener {
            // Ir al proceso de solicitud
            startActivity(Intent(this, SolicitudActivity::class.java))
        }
    }

    private fun setupRecyclerAndToggles() {
        // Inicialización y configuración simplificada de RecyclerViews
        val rvPending: RecyclerView = findViewById(R.id.rvPending)
        val rvInRoute: RecyclerView = findViewById(R.id.rvInRoute)
        val rvCompleted: RecyclerView = findViewById(R.id.rvCompleted)
        val btnTogglePending: ImageButton = findViewById(R.id.btnTogglePending)
        val btnToggleInRoute: ImageButton = findViewById(R.id.btnToggleInRoute)
        val btnToggleCompleted: ImageButton = findViewById(R.id.btnToggleCompleted)

        rvPending.layoutManager = LinearLayoutManager(this)
        rvInRoute.layoutManager = LinearLayoutManager(this)
        rvCompleted.layoutManager = LinearLayoutManager(this)

        // TODO: Asignar adaptadores a cada RecyclerView
        // rvPending.adapter = BranchSolicitudAdapter.forPending(emptyList())

        // Lógica de Toggle
        btnTogglePending.setOnClickListener { toggleVisibility(rvPending, btnTogglePending, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }
        btnToggleInRoute.setOnClickListener { toggleVisibility(rvInRoute, btnToggleInRoute, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }
        btnToggleCompleted.setOnClickListener { toggleVisibility(rvCompleted, btnToggleCompleted, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down) }

        // Asegurarse de que el RV inicialmente visible tenga el ícono correcto.
        if (rvPending.visibility == View.VISIBLE) {
            btnTogglePending.setImageResource(R.drawable.ic_arrow_up)
        }
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

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}