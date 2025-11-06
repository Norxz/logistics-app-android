package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton

/**
 * Represents the main dashboard for an administrator user.
 *
 * This activity serves as the central navigation point for administrative tasks.
 * It provides UI elements (buttons) to access different management screens, such as:
 * - Managing officials (e.g., creating, updating, deleting official accounts).
 * - Managing drivers (e.g., assigning vehicles, viewing driver status).
 * - Viewing all transportation requests.
 *
 * It also handles user session management, ensuring that only authenticated users with
 * the appropriate role (e.g., 'ANALISTA') can access this panel. If the session is
 * invalid or the role is incorrect, the user is automatically logged out and redirected
 * to the login screen.
 *
 * @see AppCompatActivity
 * @see SessionManager
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        sessionManager = SessionManager(this)

        // Asumiendo que esta pantalla es solo accesible por un rol 'ADMIN' o 'ANALISTA'
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "ANALISTA") {
            logoutUser()
            return
        }

        initViews()
    }

    private fun initViews() {
        // Mapeo de botones de tu XML activity_admin_panel
        val btnManageOfficials: MaterialButton = findViewById(R.id.btnManageOfficials)
        val btnManageDrivers: MaterialButton = findViewById(R.id.btnManageDrivers)
        val btnViewAllRequests: MaterialButton = findViewById(R.id.btnViewAllRequests)
        val btnLogoutAdmin: MaterialButton = findViewById(R.id.btnLogoutAdmin)

        // Configuración de Listeners
        btnManageOfficials.setOnClickListener {
            // TODO: Implementar la Activity de Gestión de Funcionarios/Personal
            // startActivity(Intent(this, ManageOfficialsActivity::class.java))
        }

        btnManageDrivers.setOnClickListener {
            // TODO: Implementar la Activity de Gestión de Conductores
            // startActivity(Intent(this, ManageDriversActivity::class.java))
        }

        btnViewAllRequests.setOnClickListener {
            // TODO: Implementar la Activity para Ver Todas las Solicitudes
            // startActivity(Intent(this, ViewAllRequestsActivity::class.java))
        }

        btnLogoutAdmin.setOnClickListener {
            logoutUser()
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