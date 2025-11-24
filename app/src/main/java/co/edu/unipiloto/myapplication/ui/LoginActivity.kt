package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.rest.RetrofitClient // Cliente Retrofit
import co.edu.unipiloto.myapplication.rest.LoginRequest // DTO de Request
import co.edu.unipiloto.myapplication.model.User // DTO de Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity principal para el inicio de sesión.
 * Maneja la autenticación segura de Clientes y Personal Logístico.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas
    private lateinit var btnBack: MaterialButton
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var btnForgotPassword: MaterialButton

    private var targetRole: String = "CLIENTE"

    // Repositorios y Gestores
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializar componentes
        sessionManager = SessionManager(this)

        initViews()
        setupBackButtonNavigation()

        // 3. Verificar sesión activa (Si ya está logueado, ir al dashboard)
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole())
            return
        }

        targetRole = intent.getStringExtra("TARGET_ROLE") ?: "CLIENTE"

        configureRegisterButton()

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
        btnForgotPassword = findViewById(R.id.btnForgotPassword)
    }

    private fun setupBackButtonNavigation() {
        btnBack.setOnClickListener {
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
        }
    }

    /**
     * Realiza la validación de campos y llama al servicio REST para autenticar.
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

        val loginRequest = LoginRequest(emailOrUsername, password)

        // 2. Llamar al servicio REST (Backend)
        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                // setLoadingState(false) // Si tuvieras un loading

                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!

                    sessionManager.createLoginSession(
                        userId = userData.id,
                        role = userData.role,
                        zona = userData.sucursal?.nombre,
                        name = userData.fullName,
                        email = userData.email
                    )

                    navigateToDashboard(userData.role)
                } else {
                    // 4. Autenticación fallida (401 Unauthorized, 409 Conflict, etc.)
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales incorrectas.",
                        Toast.LENGTH_LONG
                    ).show()
                    tilPassword.error = "Email/Contraseña incorrectos."
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Manejo de fallos de red o servidor no disponible
                Log.e("Login", "Error de conexión al servidor: ${t.message}")
                Toast.makeText(this@LoginActivity, "Fallo de red: ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    /**
     * Redirige al usuario a la pantalla principal correspondiente a su rol.
     * (Función movida fuera del Callback de Retrofit)
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

    private fun configureRegisterButton() {
        if (targetRole.uppercase() == "CLIENTE") {
            btnGoRegister.visibility = View.VISIBLE
        } else {
            btnGoRegister.visibility = View.GONE
        }
    }

}