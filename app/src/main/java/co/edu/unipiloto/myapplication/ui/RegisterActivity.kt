package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager // Asegúrate de que la ruta sea correcta
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Activity para el registro de nuevos usuarios.
 * Permite registrar Clientes o Personal Logístico.
 */
class RegisterActivity : AppCompatActivity() {

    // --- VISTAS ---
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPassword2: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPassword2: TextInputLayout
    private lateinit var spRol: Spinner
    private lateinit var spZona: Spinner
    private lateinit var tvZonaLabel: TextView
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var progressBar: ProgressBar

    // --- DATOS Y UTILIDADES ---
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    // Roles que requieren selección de Zona
    private val ROLES_LOGISTICOS = listOf("CONDUCTOR", "GESTOR", "FUNCIONARIO")
    private val ZONAS_DISPONIBLES = listOf("Bogotá - Norte", "Bogotá - Sur", "Bogotá - Occidente")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asumiendo que el layout se llama activity_register.xml
        setContentView(R.layout.activity_register)

        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)

        initViews()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        // Inicializar EditTexts y TextInputLayouts
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPassword2 = findViewById(R.id.etPassword2)
        tilEmail = etEmail.parent.parent as TextInputLayout // Obtener el TextInputLayout
        tilPassword = etPassword.parent.parent as TextInputLayout
        tilPassword2 = etPassword2.parent.parent as TextInputLayout

        // Inicializar Spinners y TextView de Zona
        spRol = findViewById(R.id.spRol)
        spZona = findViewById(R.id.spZona)
        tvZonaLabel = findViewById(R.id.tvZonaLabel)

        // Botones y ProgressBar
        btnGoRegister = findViewById(R.id.btnGoRegister)
        progressBar = findViewById(R.id.progress)
    }

    private fun setupSpinners() {
        // Configurar Spinner de Roles
        val roles = listOf("CLIENTE", "CONDUCTOR", "GESTOR") // Simplificado, puedes añadir más
        val rolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRol.adapter = rolAdapter

        // Configurar Spinner de Zonas
        val zonaAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, ZONAS_DISPONIBLES)
        zonaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spZona.adapter = zonaAdapter

        // Listener para la lógica de visibilidad de la Zona
        spRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedRole = parent?.getItemAtPosition(position).toString().uppercase()
                // Muestra la Zona si el rol seleccionado es logístico
                if (ROLES_LOGISTICOS.contains(selectedRole)) {
                    tvZonaLabel.visibility = View.VISIBLE
                    spZona.visibility = View.VISIBLE
                } else {
                    tvZonaLabel.visibility = View.GONE
                    spZona.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnGoRegister.setOnClickListener {
            performRegistration()
        }

        // Botón para volver al Login
        findViewById<MaterialButton>(R.id.btnGoLogin).setOnClickListener {
            finish() // Simplemente cierra esta actividad y vuelve a LoginActivity
        }
    }

    /**
     * Realiza la validación de campos y el intento de registro.
     */
    private fun performRegistration() {
        // Limpiar errores
        tilEmail.error = null
        tilPassword.error = null
        tilPassword2.error = null

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val password2 = etPassword2.text.toString()
        val role = spRol.selectedItem.toString().uppercase()
        val zona = if (spZona.visibility == View.VISIBLE) spZona.selectedItem.toString() else null

        // 1. VALIDACIÓN BÁSICA
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            // CORRECCIÓN: Usamos un mensaje directo en Toast para evitar el error de R.string
            Toast.makeText(this, "Debe llenar todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            tilEmail.error = "Formato de email inválido."
            return
        }

        if (password != password2) {
            tilPassword2.error = "Las contraseñas no coinciden."
            return
        }

        if (ROLES_LOGISTICOS.contains(role) && zona.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Debe seleccionar una zona para el rol ${role}.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 2. HASH DE CONTRASEÑA (CRÍTICO EN PRODUCCIÓN)
        val passwordHash = password

        // 3. INTENTO DE REGISTRO
        progressBar.visibility = View.VISIBLE

        val newId: Long = when (role) {
            "CLIENTE" -> {
                // Registro de CLIENTE
                userRepository.registerClient(
                    email = email,
                    passwordHash = passwordHash,
                    fullName = "Nuevo Cliente", // Placeholder
                    phoneNumber = "0" // Placeholder
                )
            }

            else -> {
                // Registro de Personal Logístico (CONDUCTOR, GESTOR, etc.)
                // ⚠️ ASUME QUE YA CREASTE EL MÉTODO registerRecolector en UserRepository
                // Se usa el email como 'username' para este personal.
                userRepository.registerRecolector(
                    username = email,
                    passwordHash = passwordHash,
                    role = role,
                    zona = zona
                )
            }
        }

        progressBar.visibility = View.GONE

        if (newId != -1L) {
            Toast.makeText(this, "Registro Exitoso como $role!", Toast.LENGTH_LONG).show()

            // 4. Iniciar sesión y navegar automáticamente
            sessionManager.createLoginSession(newId, role, zona)
            navigateToMainScreen(role)
        } else {
            Toast.makeText(
                this,
                "Error: El email ya está registrado o falló la base de datos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
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