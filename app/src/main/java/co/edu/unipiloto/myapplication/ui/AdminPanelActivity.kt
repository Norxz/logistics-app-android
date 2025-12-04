package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton

/**
 * Activity para el Panel de Administración.
 * Permite al administrador acceder a las funciones de gestión y visualización.
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // Vistas
    private lateinit var tvAdminTitle: TextView
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnManageBranches: MaterialButton
    private lateinit var btnViewAllRequests: MaterialButton
    private lateinit var btnLogoutAdmin: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        // Verificación de seguridad: solo el rol de Administrador debe acceder
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "ADMIN") {
            logoutUser()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvAdminTitle = findViewById(R.id.tvAdminTitle)

        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnManageBranches = findViewById(R.id.btnManageBranches)
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)
    }

    private fun setupListeners() {

        // 1. Botón: Gestionar Usuarios Existentes
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, LogisticUserManagementActivity::class.java)
            startActivity(intent)
        }

        // 2. Botón: Gestionar Sucursales
        btnManageBranches.setOnClickListener {
            val intent = Intent(this, ManageBranchesActivity::class.java)
            startActivity(intent)
        }

        // 3. Botón: Ver Todas las Solicitudes
        btnViewAllRequests.setOnClickListener {
            val intent = Intent(this, ViewAllRequestsActivity::class.java)
            startActivity(intent)

            Toast.makeText(
                this,
                "Navegando a Gestión de Solicitudes",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 4. Cierre de Sesión
        btnLogoutAdmin.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Limpia la sesión y redirige al usuario a la pantalla de Login.
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}