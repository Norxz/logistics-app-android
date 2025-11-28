package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.databinding.ActivityLoginBinding // ⬅️ Importar el View Binding
import co.edu.unipiloto.myapplication.dto.LoginRequest
import co.edu.unipiloto.myapplication.dto.UserResponse
import co.edu.unipiloto.myapplication.repository.AuthRepository
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.viewmodel.AuthState
import co.edu.unipiloto.myapplication.viewmodel.AuthViewModel
import co.edu.unipiloto.myapplication.viewmodel.AuthViewModelFactory
import co.edu.unipiloto.myapplication.R // Para acceder a tus recursos de strings

/**
 * 🔑 Activity de inicio de sesión, implementada siguiendo el patrón MVVM.
 * Utiliza el AuthViewModel para manejar la lógica de autenticación y Retrofit/Repository.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas y ViewModel
    private lateinit var binding: ActivityLoginBinding // Objeto de View Binding
    private lateinit var authViewModel: AuthViewModel

    // Gestores
    private lateinit var sessionManager: SessionManager

    // Rol de destino requerido (ej. "CONDUCTOR", "FUNCIONARIO")
    private var requiredRole: String = "CLIENTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Inicializar utilidades y obtener el rol
        sessionManager = SessionManager(this)
        requiredRole = intent.getStringExtra(MainActivity.EXTRA_TARGET_ROLE)?.uppercase() ?: "CLIENTE"

        // 3. Inicializar ViewModel (Inyección manual de dependencias)
        initViewModel()

        // 4. Configurar UI y Listeners
        configureUIByRole()
        setupListeners()
        setupObservers()

        // 5. Verificar sesión activa (Si ya está logueado, ir al dashboard)
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole())
        }
    }

    private fun initViewModel() {
        val authApi = RetrofitClient.getAuthApi() // Asume que tienes un getter para AuthApi
        val authRepository = AuthRepository(authApi)
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    // --- Configuración de UI y Listeners ---

    private fun configureUIByRole() {
        // Ocultar botón de Registro si el acceso no es para Cliente
        if (requiredRole != "CLIENTE") {
            binding.btnGoRegister.visibility = View.GONE
        }
        // TODO: Actualizar tvLoginTitle (se recomienda manejar esto en strings.xml)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnLogin.setOnClickListener { performLogin() }

        binding.btnGoRegister.setOnClickListener {
            // Navegar a la Activity de Registro
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Lógica de Login MVVM ---

    private fun performLogin() {
        // Limpiar errores (Usando View Binding)
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val emailOrUsername = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (emailOrUsername.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_required_field)
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_required_field)
            return
        }

        val loginRequest = LoginRequest(emailOrUsername, password)

        // 🚨 Llamada al ViewModel, no al Retrofit directo
        authViewModel.login(loginRequest)
    }

    // --- Observadores ---

    private fun setupObservers() {
        // Observar estado de carga (opcional: para mostrar ProgressBar)
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            // Aquí puedes mostrar/ocultar un ProgressBar
        }

        // 🚨 Observar el resultado de la autenticación
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Success -> handleLoginSuccess(state.user)
                is AuthState.Error -> handleLoginError(state.message)
                is AuthState.Idle -> { /* Estado de reposo */ }
            }
        }
    }

    // --- Manejo de Resultados ---

    /**
     * Maneja la respuesta exitosa del ViewModel.
     */
    private fun handleLoginSuccess(user: UserResponse?) {
        authViewModel.clearState() // Limpiar el estado para evitar reprocesos

        val userRole = user?.role?.uppercase()

        if (user != null && userRole != null && isRoleAuthorized(userRole, requiredRole)) {

            sessionManager.createLoginSession(
                userId = user.id,
                role = user.role,
                sucursal = user.sucursal?.nombre,
                name = user.fullName,
                email = user.email
            )
            Toast.makeText(this, "Acceso exitoso como ${user.role}", Toast.LENGTH_LONG).show()

            navigateToDashboard(user.role)

        } else if (user != null && userRole != null) {
            // Rol no autorizado para esta entrada
            val msg = "Acceso denegado. Tu rol ($userRole) no está autorizado para esta sección."
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            binding.tilPassword.error = "Rol no autorizado."
            binding.etPassword.text?.clear()
        } else {
            // Error genérico
            handleLoginError("Error en datos de usuario tras login exitoso.")
        }
    }

    /**
     * Maneja la respuesta de error del ViewModel (incluye errores de red/servidor).
     */
    private fun handleLoginError(errorMessage: String) {
        authViewModel.clearState()
        Toast.makeText(this, "Error de Login: $errorMessage", Toast.LENGTH_LONG).show()
        Log.e("LOGIN_ERROR", errorMessage)
        binding.tilPassword.error = errorMessage
        binding.etPassword.text?.clear()
    }

    // --- Lógica de Roles Agrupados ---

    /**
     * 🔑 Verifica si el rol real del usuario logueado es válido para el rol de acceso requerido.
     */
    private fun isRoleAuthorized(userRole: String, requiredRole: String): Boolean {
        val upperUserRole = userRole.uppercase()
        val upperRequiredRole = requiredRole.uppercase()

        return when (upperRequiredRole) {
            // Botón "Soy Conductor" (Acepta CONDUCTOR o GESTOR)
            "CONDUCTOR" -> upperUserRole == "CONDUCTOR" || upperUserRole == "GESTOR"

            // Botón "Soy Funcionario" (Acepta FUNCIONARIO o ANALISTA)
            "FUNCIONARIO" -> upperUserRole == "FUNCIONARIO" || upperUserRole == "ANALISTA"

            // El resto debe coincidir exactamente (CLIENTE, ADMIN)
            else -> upperUserRole == upperRequiredRole
        }
    }

    // --- Navegación ---

    private fun navigateToDashboard(role: String) {
        val intent = when (role.uppercase()) {
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            "CONDUCTOR" -> Intent(this, DriverDashboardActivity::class.java)
            "GESTOR", "ANALISTA", "FUNCIONARIO" -> Intent(this, ManagerDashboardActivity::class.java) // Agrupados
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