package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.ui.*


/**
 * Activity de Bienvenida, mapeada al layout activity_main.xml.
 * Es la primera pantalla que aparece, maneja la redirección de sesión activa
 * y la navegación a Login, Registro, Rastreo, y accesos específicos.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Usamos activity_main.xml para el welcome/landing

        sessionManager = SessionManager(this)

        // 1. Verificar sesión activa y redirigir
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole())
            return
        }

        // 2. Configurar Listeners
        setupListeners()
    }

    private fun setupListeners() {
        // Mapeo de botones de activity_main.xml
        findViewById<MaterialButton>(R.id.btnCheckStatus).setOnClickListener {
            // Ir a la pantalla de rastreo (TrackShippingActivity)
            startActivity(Intent(this, TrackShippingActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnRequestShipping).setOnClickListener {
            // Ir a la pantalla de Login (para Clientes)
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Acceso de Personal
        findViewById<MaterialButton>(R.id.btnOfficials).setOnClickListener {
            // Funcionarios/Personal de Sucursal -> Usan el Login ÚNICO
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnDrivers).setOnClickListener {
            // Conductores -> Usan el Login ÚNICO
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAdmin).setOnClickListener {
            // Administración/Analista -> Usan el Login ÚNICO
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun navigateToDashboard(role: String) {
        val destClass = when (role.uppercase()) {
            "CLIENTE" -> MainActivity::class.java
            "CONDUCTOR" -> DriverDashboardActivity::class.java
            "GESTOR" -> ManagerDashboardActivity::class.java
            "FUNCIONARIO" -> BranchDashboardActivity::class.java
            "ANALISTA" -> AdminPanelActivity::class.java
            else -> LoginActivity::class.java
        }
        val intent = Intent(this, destClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}