package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import android.util.Log
import co.edu.unipiloto.myapplication.db.UserRepository

/**
 * Activity Principal (Hub de Bienvenida).
 * Esta pantalla sirve como el punto de entrada, manejando la navegación inicial
 * y la verificación de sesiones activas.
 */
class MainActivity : AppCompatActivity() {

    // --- VISTAS ---
    private lateinit var btnCheckStatus: MaterialButton
    private lateinit var btnRequestShipping: MaterialButton
    private lateinit var btnOfficials: MaterialButton
    private lateinit var btnDrivers: MaterialButton
    private lateinit var btnAdmin: MaterialButton

    // --- UTILIDADES ---
    private lateinit var sessionManager: SessionManager

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ocultar la barra de acción (opcional, ya que este es un hub)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        // 🏆 INICIALIZACIÓN AÑADIDA
        userRepository = UserRepository(this)

        initViews()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // 🏆 VERIFICACIÓN CRÍTICA: Si ya está logueado, saltar el Hub de Bienvenida.
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getRole()
            Log.d("MainActivity", "Session found. Redirecting role: $role")
            navigateToDashboard(role)
        }
    }


    private fun initViews() {
        btnCheckStatus = findViewById(R.id.btnCheckStatus)
        btnRequestShipping = findViewById(R.id.btnRequestShipping)
        btnOfficials = findViewById(R.id.btnOfficials)
        btnDrivers = findViewById(R.id.btnDrivers)
        btnAdmin = findViewById(R.id.btnAdmin)
    }

    private fun setupListeners() {

        // 1. CONSULTAR ESTADO (Tracking Activity)
        btnCheckStatus.setOnClickListener {
            // 🚨 DEBES CREAR TrackingActivity.kt (Para seguimiento de guías sin login)
            startActivity(Intent(this, TrackShippingActivity::class.java))
        }

        // 2. SOLICITAR ENVÍO (Requiere Login/Registro)
        btnRequestShipping.setOnClickListener {
            // Lleva al Login. La LoginActivity debe manejar la redirección a RegisterActivity.
            startActivity(Intent(this, LoginActivity::class.java).apply {
                // Puedes pasar un extra para que Login sepa que el objetivo es un Envío (opcional)
                putExtra("TARGET_ROLE", "CLIENTE")
            })
        }

        // 3. ACCESO PERSONAL LOGÍSTICO (Oficiales/Funcionarios)
        btnOfficials.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                // Indicamos que el acceso es para Funcionarios
                putExtra("TARGET_ROLE", "FUNCIONARIO")
            })
        }

        // 4. ACCESO CONDUCTORES
        btnDrivers.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                // Indicamos que el acceso es para Conductores
                putExtra("TARGET_ROLE", "CONDUCTOR")
            })
        }

        // 5. ACCESO ADMINISTRADOR
        btnAdmin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                // Indicamos que el acceso es para el Admin
                putExtra("TARGET_ROLE", "ADMIN")
            })
        }
    }

    /**
     * Navega al dashboard correspondiente al rol.
     * Esta función es la que te permite saltar el Hub de Bienvenida al iniciar la app.
     */
    private fun navigateToDashboard(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(
                this,
                ClientDashboardActivity::class.java
            ) // ⚠️ Renombrado para no confundir con este hub
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)
            "GESTOR" -> Intent(this, ManagerDashboardActivity::class.java)
            "FUNCIONARIO" -> Intent(this, BranchDashboardActivity::class.java)
            "ADMIN" -> Intent(this, AdminDashboardActivity::class.java) // 🚨 Asumimos la existencia
            else -> {
                Toast.makeText(this, "Rol no reconocido. Cerrando sesión.", Toast.LENGTH_LONG)
                    .show()
                sessionManager.logoutUser()
                Intent(this, MainActivity::class.java) // Vuelve al Hub
            }
        }

        // Estas flags son CRÍTICAS: eliminan el historial de navegación para que el usuario no pueda "regresar" al Login/Hub
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}