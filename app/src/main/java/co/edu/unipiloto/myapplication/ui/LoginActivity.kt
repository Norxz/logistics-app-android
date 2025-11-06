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
import java.security.MessageDigest // Para el Hashing

/**
 * Activity principal para el inicio de sesión.
 * Maneja la autenticación segura de Clientes y Personal Logístico.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton

    // Repositorios y Gestores
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    /**
     * Initializes the login screen: sets the layout, prepares repositories and session manager,
     * configures the toolbar and view bindings, checks for an existing session to redirect
     * to the appropriate dashboard, and sets up UI listeners for login and registration.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializar componentes
        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)

        // 2. Configurar el botón de regreso y el título en la Toolbar
        setupToolbarBackNavigation()

        initViews()

        // 3. Verificar sesión activa (Si ya está logueado, ir al dashboard)
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole())
            return
        }

        // 4. Configurar Listeners
        setupListeners()
    }

    /**
     * Configure the native ActionBar to show the up/back button and set the activity title to the login screen string.
     *
     * Sets the ActionBar's displayHomeAsUpEnabled to true and applies the `R.string.login_title` resource as the title when an ActionBar is present.
     */
    private fun setupToolbarBackNavigation() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // Ya asumimos que el recurso R.string.login_title existe
            title = getString(R.string.login_title)
        }
    }

    /**
     * Handle toolbar home/up button presses by navigating back to MainActivity.
     *
     * When the home/up item is selected this starts MainActivity with
     * FLAG_ACTIVITY_CLEAR_TOP, finishes the current activity, and returns control
     * to the previous task state.
     *
     * @return `true` if the home/up item was handled and navigation started, `false` otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Regresa a MainActivity (el Hub de Bienvenida)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Binds the activity's UI fields to their corresponding views in the current layout.
     *
     * Initializes the TextInputLayouts, TextInputEditTexts, and Buttons used for email,
     * password, login, and navigation to registration so they can be accessed elsewhere
     * in the activity.
     */
    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
    }

    /**
     * Attach click handlers for the login and register buttons.
     *
     * Sets the login button to invoke performLogin() and the register button to open RegisterActivity.
     */
    private fun setupListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Validates input and attempts to authenticate the user, creating a session and navigating on success.
     *
     * If email/username or password fields are empty, marks the corresponding input with a required-field error and aborts.
     * Otherwise hashes the password and delegates authentication to the user repository.
     * On successful authentication creates a login session with the user's id, role and zona, shows a welcome toast, and navigates to the role-specific dashboard.
     * On authentication failure shows an error toast and marks the password input with an incorrect-credentials error.
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

        // 🏆 APLICAR HASHING (debe coincidir con RegisterActivity)
        val passwordHash = hashPassword(password)

        // 🏆 LLAMAR al método login() del repositorio que usa el hash
        val userData = userRepository.login(emailOrUsername, passwordHash)

        if (userData != null) {
            // Autenticación exitosa. userData es UserSessionData.
            sessionManager.createLoginSession(
                userId = userData.id,
                role = userData.role,
                zona = userData.zona
            )
            Toast.makeText(this, "Bienvenido, ${userData.role}!", Toast.LENGTH_SHORT).show()
            navigateToDashboard(userData.role)
        } else {
            // Autenticación fallida
            Toast.makeText(this, "Credenciales incorrectas o usuario inactivo.", Toast.LENGTH_LONG).show()
            tilPassword.error = "Email/Contraseña incorrectos."
        }
    }

    /**
     * Produce a SHA-256 hex string for the given password.
     *
     * @param password The plaintext password to hash.
     * @return The SHA-256 digest of `password` encoded as a lowercase hex string, or the original `password` if hashing fails.
     */
    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("Security", "Error hashing password in Login: ${e.message}")
            password
        }
    }

    /**
     * Navigate the user to the main dashboard corresponding to their role.
     *
     * Maps the provided role (case-insensitive) to the appropriate Activity:
     * - "CLIENTE" -> ClientDashboardActivity
     * - "CONDUCTOR" -> DriverDashboardActivity
     * - "GESTOR" -> ManagerDashboardActivity
     * - "FUNCIONARIO" -> BranchDashboardActivity
     * - "ANALISTA", "ADMIN" -> AdminDashboardActivity
     *
     * If the role is unrecognized, the current session is cleared and the LoginActivity is shown.
     * The target activity is started with flags that clear the existing task and the current activity is finished.
     *
     * @param role The user's role string; comparison is case-insensitive.
     */
    private fun navigateToDashboard(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)
            "GESTOR" -> Intent(this, ManagerDashboardActivity::class.java)
            "FUNCIONARIO" -> Intent(this, BranchDashboardActivity::class.java)
            "ANALISTA", "ADMIN" -> Intent(this, AdminDashboardActivity::class.java) // Asumimos un dashboard para roles administrativos
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