package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager // Asegúrate de que esta clase exista
import com.google.android.material.button.MaterialButton

/**
 * Activity para el Panel de Administración.
 * Permite al administrador acceder a las funciones de gestión de usuarios logísticos y solicitudes.
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // Vistas
    private lateinit var btnRegisterPersonnel: MaterialButton
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnViewAllRequests: MaterialButton
    private lateinit var btnLogoutAdmin: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        // Ocultar la barra de acción por consistencia
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        // Verificación de seguridad: solo el rol de Administrador debe acceder
        // Asumo que el rol 'ADMIN' está almacenado correctamente tras el login.
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "ADMIN") {
            // Si no es el rol correcto o no está logeado, forzar el logout
            logoutUser()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnRegisterPersonnel = findViewById(R.id.btnRegisterPersonnel)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)
    }

    private fun setupListeners() {

        // 1. Botón: Registrar Nuevo Usuario Logístico (Lanza RegisterActivity en modo Admin)
        btnRegisterPersonnel.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            // Usa la constante definida en RegisterActivity para activar el modo de registro de personal
            intent.putExtra(RegisterActivity.EXTRA_IS_ADMIN_REGISTER, true)
            startActivity(intent)
        }

        // 2. Botón: Gestionar Usuarios Existentes (Redirige a la Activity de gestión logística)
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, LogisticUserManagementActivity::class.java)
            startActivity(intent)
        }

        // 3. Botón: Ver Todas las Solicitudes
        btnViewAllRequests.setOnClickListener {
            // TODO: Crear e implementar la Activity para ver las solicitudes
            Toast.makeText(this, "Navegando a Ver Todas las Solicitudes (Pendiente)", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, LoginActivity::class.java)
        // Estas flags aseguran que el historial de actividades se borre
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}