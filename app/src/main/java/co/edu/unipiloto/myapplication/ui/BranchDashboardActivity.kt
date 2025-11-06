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

    /**
     * Initializes the activity: inflates the layout, enforces that the current session belongs to a
     * user with role "FUNCIONARIO", and configures recyclers and UI listeners.
     *
     * If the user is not logged in or does not have the "FUNCIONARIO" role, the method invokes
     * logoutUser() and exits without completing setup.
     *
     * @param savedInstanceState If non-null, this Activity is being re-created from a previous saved state.
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

    /**
     * Wires UI actions for the dashboard's buttons.
     *
     * Attaches a click listener to the logout button that signs the user out, and a click listener
     * to the new-request button that starts SolicitudActivity.
     */
    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            logoutUser()
        }

        findViewById<MaterialButton>(R.id.btnNewRequest).setOnClickListener {
            // Ir al proceso de solicitud
            startActivity(Intent(this, SolicitudActivity::class.java))
        }
    }

    /**
     * Sets up the three branch request lists (pending, in-route, completed) and their toggle controls.
     *
     * Configures layout managers for each RecyclerView, attaches click listeners on the toggle
     * buttons to show/hide the corresponding lists and update toggle icons, and ensures the
     * pending list's toggle icon matches its initial visibility. Adapters for the lists are left
     * to be assigned (TODO).
     */
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

    /**
     * Toggle visibility of a RecyclerView and update the toggle button's icon accordingly.
     *
     * @param rv The RecyclerView to show or hide.
     * @param btn The ImageButton whose icon will be updated.
     * @param iconUp Resource id for the icon to display when `rv` is visible.
     * @param iconDown Resource id for the icon to display when `rv` is hidden.
     */
    private fun toggleVisibility(rv: RecyclerView, btn: ImageButton, iconUp: Int, iconDown: Int) {
        if (rv.visibility == View.VISIBLE) {
            rv.visibility = View.GONE
            btn.setImageResource(iconDown)
        } else {
            rv.visibility = View.VISIBLE
            btn.setImageResource(iconUp)
        }
    }

    /**
     * Logs out the current session, navigates to the login screen, and clears the activity back stack.
     *
     * Calls the session manager to remove session data, starts LoginActivity with flags that
     * create a new task and clear existing tasks, and finishes this activity.
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}