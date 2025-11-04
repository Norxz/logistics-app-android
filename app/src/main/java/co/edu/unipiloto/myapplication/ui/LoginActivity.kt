package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton

/**
 * Activity principal para el inicio de sesión.
 * Maneja la autenticación de Clientes y Personal Logístico.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas del layout activity_login.xml
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var btnGoBack: MaterialButton // Asumiendo que ImageButton actúa como botón de regreso

    // Repositorios y Gestores
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializar componentes
        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)
        initViews()

        // 2. Verificar sesión activa
        if (sessionManager.isLoggedIn()) {
            // Si ya hay sesión, redirigir inmediatamente
            navigateToMainScreen(sessionManager.getRole())
            return
        }

        // 3. Configurar Listeners
        setupListeners()
    }

    /**
     * Inicializa las vistas mapeándolas desde el layout.
     */
    private fun initViews() {
        // En tu layout: tilEmail, etEmail, tilPassword, etPassword, btnLogin, btnGoRegister, btnGoBack
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)

        // El layout usa un ImageButton (btnGoBack), pero para simplificar, lo tratamos como View/Button
        findViewById<View>(R.id.btnGoBack).setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java) // Redirigir a la Activity principal
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Limpia las actividades encima
            startActivity(intent)
            finish()
        }
    }

    /**
     * Configura los listeners para los botones de la interfaz.
     */
    private fun setupListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        btnGoRegister.setOnClickListener {
            // Redirigir a la Activity de registro
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Puedes agregar listener para btnForgotPassword aquí si es necesario
    }

    /**
     * Realiza la validación de campos y el intento de autenticación.
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

        // ⚠️ Nota: En una app real, la contraseña debe ser HASHED antes de pasarla al repositorio
        // Aquí usamos la contraseña tal cual, asumiendo que el repositorio la compara
        // directamente con el hash almacenado, o que la aplicación no usa hashing (no recomendado).
        val passwordHash = password // Usar una función de hashing real aquí (ej. BCrypt)

        val userData = userRepository.login(emailOrUsername, passwordHash)

        if (userData != null) {
            // Autenticación exitosa
            sessionManager.createLoginSession(
                userId = userData.id,
                role = userData.role,
                zona = userData.zona
            )
            Toast.makeText(this, "Bienvenido, ${userData.role}!", Toast.LENGTH_SHORT).show()
            navigateToMainScreen(userData.role)
        } else {
            // Autenticación fallida
            Toast.makeText(this, "Credenciales incorrectas o usuario inactivo.", Toast.LENGTH_LONG).show()
            tilPassword.error = "Email/Contraseña incorrectos."
        }
    }

    /**
     * Redirige al usuario a la pantalla principal correspondiente a su rol.
     *
     * @param role El rol del usuario obtenido de la sesión (CLIENTE, CONDUCTOR, GESTOR, etc.).
     */
    private fun navigateToMainScreen(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(this, MainActivity::class.java)
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)

            // Roles específicos del personal logístico:
            "GESTOR" -> Intent(this, ManagerDashboardActivity::class.java)
            "FUNCIONARIO" -> Intent(this, BranchDashboardActivity::class.java)
            "ANALISTA" -> Intent(this, ManagerDashboardActivity::class.java) // Asumimos que Analista usa el mismo dashboard que Manager

            else -> {
                // Rol no reconocido, lo enviamos de vuelta al Login después de cerrar sesión por seguridad
                sessionManager.logoutUser()
                Intent(this, LoginActivity::class.java)
            }
        }

        // Estas flags aseguran que la nueva Activity sea la raíz y borre el historial de navegación (login/registro)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}