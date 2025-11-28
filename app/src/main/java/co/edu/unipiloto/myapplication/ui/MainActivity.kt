package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.databinding.ActivityMainBinding
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * 🏠 Activity Principal (Hub de Bienvenida).
 * Punto de entrada que maneja la navegación inicial y verifica sesiones activas.
 */
class MainActivity : AppCompatActivity() {

    // 1. Reemplazamos las variables de vistas individuales por el objeto de Binding
    private lateinit var binding: ActivityMainBinding

    // Utilidad para gestionar la sesión del usuario
    private lateinit var sessionManager: SessionManager

    // Constante para definir el rol de destino en la LoginActivity
    companion object {
        const val EXTRA_TARGET_ROLE = "TARGET_ROLE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inicializar el View Binding y establecer la vista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        // 3. Ya no necesitamos initViews()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // 🏆 VERIFICACIÓN CRÍTICA: Si ya está logueado, saltar el Hub de Bienvenida.
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getRole()
            Log.d("MainActivity", "Sesión activa encontrada. Redirigiendo al Dashboard. Rol: $role")
            navigateToDashboard(role)
        }
    }

    /**
     * Configura los listeners para los botones de la pantalla principal,
     * utilizando el objeto 'binding' para acceder a las vistas.
     */
    private fun setupListeners() {

        // 1. CONSULTAR ESTADO (Tracking Activity - No requiere login)
        binding.btnCheckStatus.setOnClickListener {
            // TODO: Iniciar TrackShippingActivity.kt
            startActivity(Intent(this, TrackShippingActivity::class.java))
        }

        // 2. SOLICITAR ENVÍO (Acceso CLIENTE)
        binding.btnRequestShipping.setOnClickListener {
            navigateToLogin("CLIENTE")
        }

        // 3. ACCESO FUNCIONARIO (Target: FUNCIONARIO / ANALISTA)
        binding.btnOfficials.setOnClickListener {
            navigateToLogin("FUNCIONARIO")
        }

        // 4. ACCESO CONDUCTORES (Target: CONDUCTOR / GESTOR)
        binding.btnDrivers.setOnClickListener {
            navigateToLogin("CONDUCTOR")
        }

        // 5. ACCESO ADMINISTRADOR
        binding.btnAdmin.setOnClickListener {
            navigateToLogin("ADMIN")
        }
    }

    /**
     * Inicia la LoginActivity, pasando el rol de acceso requerido.
     */
    private fun navigateToLogin(role: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(EXTRA_TARGET_ROLE, role)
        }
        startActivity(intent)
    }

    /**
     * 🧠 Navega al dashboard correspondiente al rol REAL del usuario.
     * Aquí se asegura que roles como GESTOR y ANALISTA vayan al mismo panel de gestión.
     */
    private fun navigateToDashboard(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)

            // CONDUCTOR va a su propio dashboard
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)

            // 🚨 GESTOR y ANALISTA van al Dashboard de Manager/Funcionario
            "GESTOR", "FUNCIONARIO", "ANALISTA" -> Intent(this, ManagerDashboardActivity::class.java)

            "ADMIN" -> Intent(this, AdminPanelActivity::class.java)
            else -> {
                // Rol desconocido o inválido, forzar cierre de sesión
                Toast.makeText(this, "Rol desconocido ($role). Cerrando sesión.", Toast.LENGTH_LONG).show()
                sessionManager.logoutUser()
                Intent(this, MainActivity::class.java)
            }
        }

        // Flags críticas para eliminar el historial de navegación
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}