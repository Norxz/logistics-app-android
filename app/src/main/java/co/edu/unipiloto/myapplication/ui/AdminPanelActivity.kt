package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView // Importamos TextView
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
    private lateinit var tvAdminTitle: TextView // Declaramos el TextView del título
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnViewAllRequests: MaterialButton
    private lateinit var btnLogoutAdmin: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        // Ocultar la barra de acción por defecto si el tema la muestra
        supportActionBar?.hide() // Mantenemos esta línea ya que quitamos el Toolbar

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
        // CORREGIDO: Inicializamos el TextView usando el ID del layout nuevo
        tvAdminTitle = findViewById(R.id.tvAdminTitle)

        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)
    }

    private fun setupListeners() {

        // 1. Botón: Gestionar Usuarios Existentes (Redirige a la Activity de gestión CRUD)
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, LogisticUserManagementActivity::class.java)
            startActivity(intent)
        }

        // 2. Botón: Ver Todas las Solicitudes (Próximo paso de implementación)
        btnViewAllRequests.setOnClickListener {
            // ¡IMPLEMENTACIÓN PENDIENTE!
            // Navegaremos a la nueva actividad que crearemos: ViewAllRequestsActivity
            val intent = Intent(this, ViewAllRequestsActivity::class.java)
            startActivity(intent)

            Toast.makeText(
                this,
                "Navegando a Gestión de Solicitudes",
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