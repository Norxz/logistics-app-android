package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton

/**
 * Activity para el Panel de Administración.
 * Permite al administrador acceder a las funciones de gestión y visualización.
 * Nota: Todas las funciones de registro de personal se consolidaron en LogisticUserManagementActivity.
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // Vistas
    // btnRegisterPersonnel fue eliminado
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnViewAllRequests: MaterialButton
    private lateinit var btnLogoutAdmin: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        // Ocultar la barra de acción por defecto si el tema la muestra
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        // Verificación de seguridad: solo el rol de Administrador debe acceder
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "ADMIN") {
            // Si no es el rol correcto o no está logeado, forzar el logout
            logoutUser()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Inicialización de la vista del título (aunque no se usa aquí, valida el ID)
        findViewById<android.widget.TextView>(R.id.adminTitle)

        // El botón de registro fue eliminado del layout y, por lo tanto, de aquí.
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)
    }

    private fun setupListeners() {

        // 1. Botón: Gestionar Usuarios Existentes (Redirige a la Activity de gestión CRUD)
        // Esta actividad ahora maneja tanto la lectura/edición/eliminación como la creación (vía FAB).
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, LogisticUserManagementActivity::class.java)
            startActivity(intent)
        }

        // 2. Botón: Ver Todas las Solicitudes
        btnViewAllRequests.setOnClickListener {
            // TODO: Crear e implementar la Activity para ver las solicitudes
            Toast.makeText(
                this,
                "Navegando a Ver Todas las Solicitudes (Pendiente)",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 3. Cierre de Sesión
        btnLogoutAdmin.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Limpia la sesión y redirige al usuario a la pantalla de Login.
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        // Estas flags aseguran que el historial de actividades se borre
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}