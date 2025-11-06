package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import java.security.MessageDigest

/**
 * Activity principal para el inicio de sesión.
 * Maneja la autenticación segura de Clientes y Personal Logístico.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas
    private lateinit var btnBack: MaterialButton // 🏆 VISTA AÑADIDA: Botón de regreso explícito
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var btnForgotPassword: MaterialButton // Si existe en el layout, es bueno tenerlo.

    // Repositorios y Gestores
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializar componentes
        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)

        initViews() // Inicializa todas las vistas incluyendo el nuevo btnBack

        // 2. 🏆 CORRECCIÓN: Configurar el listener del botón de regreso explícito
        setupBackButtonNavigation()

        // 3. Verificar sesión activa (Si ya está logueado, ir al dashboard)
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole())
            return
        }

        // 4. Configurar Listeners
        setupListeners()
    }

    private fun initViews() {
        // Inicializar el botón de regreso explícito
        btnBack = findViewById(R.id.btnBack)

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
        btnForgotPassword = findViewById(R.id.btnForgotPassword) // Aseguramos que existe
    }

    /**
     * 🏆 NUEVA IMPLEMENTACIÓN: Configura el listener para el botón explícito de regreso.
     * La lógica de Toolbar (setupToolbarBackNavigation y onOptionsItemSelected) fue eliminada.
     */
    private fun setupBackButtonNavigation() {
        btnBack.setOnClickListener {
            // Regresa a la actividad anterior, que en este flujo suele ser MainActivity (Hub de Bienvenida)
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnForgotPassword.setOnClickListener {
            Toast.makeText(
                this,
                "Funcionalidad de Recuperación de Contraseña no implementada.",
                Toast.LENGTH_SHORT
            ).show()
            // Aquí iría el intent a ForgotPasswordActivity
        }
    }

    /**
     * Realiza la validación de campos, hashea la contraseña y llama al repositorio para autenticar.
     */
    private fun performLogin() {
        tilEmail.error = null
        tilPassword.error = null

        val emailOrUsername = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (emailOrUsername.isEmpty()) {
            tilEmail.error = getString(R.string.error_required_field)
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_required_field)
            return
        }

        // 🏆 APLICAR HASHING
        val passwordHash = hashPassword(password)

        // 🏆 LLAMAR al método login() del repositorio que usa el hash
        val userData = userRepository.login(emailOrUsername, passwordHash)

        if (userData != null) {
            // Autenticación exitosa.
            sessionManager.createLoginSession(
                userId = userData.id,
                role = userData.role,
                zona = userData.sucursal,
                name = userData.name
            )
            Toast.makeText(this, "Bienvenido, ${userData.name}!", Toast.LENGTH_SHORT).show()
            navigateToDashboard(userData.role)
        } else {
            // Autenticación fallida
            Toast.makeText(this, "Credenciales incorrectas o usuario inactivo.", Toast.LENGTH_LONG)
                .show()
            tilPassword.error = "Email/Contraseña incorrectos."
        }
    }

    /**
     * Genera un hash SHA-256 de la contraseña.
     * Es idéntico al usado en RegisterActivity y debe coincidir con el hash de la DB.
     */
    private fun hashPassword(password: String): String {
        return try {
            // Obtener la instancia del algoritmo de hashing
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())

            // Formatear el array de bytes a un String hexadecimal de 64 caracteres
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("Security", "Error hashing password: ${e.message}")
            // En caso de error, retorna la contraseña sin hashear (fallará la autenticación)
            password
        }
    }

    /**
     * Redirige al usuario a la pantalla principal correspondiente a su rol.
     */
    private fun navigateToDashboard(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)
            "GESTOR" -> Intent(this, ManagerDashboardActivity::class.java)
            "FUNCIONARIO", "ANALISTA" -> Intent(this, BranchDashboardActivity::class.java)
            "ADMIN" -> Intent(this, AdminPanelActivity::class.java)
            else -> {
                sessionManager.logoutUser()
                Intent(this, LoginActivity::class.java)
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}