package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import co.edu.unipiloto.myapplication.security.PasswordHasher // 🏆 IMPORTANTE: Usar la clase de seguridad
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Activity para el registro de nuevos usuarios.
 * Permite registrar Clientes o Personal Logístico.
 */
class RegisterActivity : AppCompatActivity() {

    // --- CONSTANTES ---
    companion object {
        const val EXTRA_IS_ADMIN_REGISTER = "IS_ADMIN_REGISTER"
        const val ROL_CLIENTE = "CLIENTE"
        const val ROL_ADMIN = "ADMIN"
    }

    // --- VISTAS ---
    private lateinit var etFullName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPassword2: TextInputEditText

    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilPhoneNumber: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPassword2: TextInputLayout

    private lateinit var spRol: Spinner
    private lateinit var spZona: Spinner
    private lateinit var tvRolLabel: TextView
    private lateinit var tvZonaLabel: TextView
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var progressBar: ProgressBar

    // --- DATOS Y UTILIDADES ---
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    // Roles que requieren selección de Zona
    private val ROLES_LOGISTICOS = listOf("CONDUCTOR", "GESTOR", "FUNCIONARIO", "ANALISTA")

    // El admin puede registrar todos excepto él mismo (ya que los admins suelen ser estáticos)
    private val ADMIN_REGISTERABLE_ROLES = listOf(ROL_CLIENTE) + ROLES_LOGISTICOS.distinct()
    private val ZONAS_DISPONIBLES = listOf("Bogotá - Norte", "Bogotá - Sur", "Bogotá - Occidente")
    private val PASSWORD_BLACKLIST =
        listOf("password", "123456", "qwerty", "admin", "unipiloto", "piloto")

    // --- ESTADO ---
    private var isAdminRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)

        isAdminRegister = intent.getBooleanExtra(EXTRA_IS_ADMIN_REGISTER, false)

        initViews()
        setupSpinners()
        setupRegistrationFlowUI()
        setupListeners()
    }

    private fun initViews() {
        // Inicializar nuevos campos
        etFullName = findViewById(R.id.etFullName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        tilFullName = findViewById(R.id.tilFullName)
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber)

        // Inicializar EditTexts existentes
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPassword2 = findViewById(R.id.etPassword2)

        // Inicializar TextInputLayouts existentes (asumiendo IDs directos como corregimos)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilPassword2 = findViewById(R.id.tilPassword2)

        spRol = findViewById(R.id.spRol)
        spZona = findViewById(R.id.spZona)
        tvRolLabel = findViewById(R.id.tvRolLabel)
        tvZonaLabel = findViewById(R.id.tvZonaLabel)

        btnGoRegister = findViewById(R.id.btnGoRegister)
        progressBar = findViewById(R.id.progress)
    }

    private fun setupRegistrationFlowUI() {
        if (!isAdminRegister) {
            // Flujo Público (solo cliente)
            tvRolLabel.visibility = View.GONE
            spRol.visibility = View.GONE
            tvZonaLabel.visibility = View.GONE
            spZona.visibility = View.GONE

        } else {
            // Flujo Admin: Inicializar visibilidad de campos de cliente/zona
            // Esto asegura que al iniciar la Activity en modo Admin, la UI refleje el rol por defecto (CLIENTE) o el primero de la lista.
            val initialRole = ADMIN_REGISTERABLE_ROLES.firstOrNull() ?: ROL_CLIENTE

            val isLogistic = ROLES_LOGISTICOS.contains(initialRole)

            tvZonaLabel.visibility = if (isLogistic) View.VISIBLE else View.GONE
            spZona.visibility = if (isLogistic) View.VISIBLE else View.GONE

            tilFullName.visibility = if (isLogistic) View.GONE else View.VISIBLE
            tilPhoneNumber.visibility = if (isLogistic) View.GONE else View.VISIBLE
        }
    }

    private fun setupSpinners() {
        // Configurar Spinner de Roles
        val rolesToShow = if (isAdminRegister) ADMIN_REGISTERABLE_ROLES else listOf(ROL_CLIENTE)

        val rolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rolesToShow)
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRol.adapter = rolAdapter

        // Configurar Spinner de Zonas
        val zonaAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, ZONAS_DISPONIBLES)
        zonaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spZona.adapter = zonaAdapter

        // Listener para la lógica de visibilidad de Rol, Zona, y campos de Cliente
        if (isAdminRegister) {
            spRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedRole = parent?.getItemAtPosition(position).toString().uppercase()

                    // Lógica para Zona (para roles logísticos)
                    if (ROLES_LOGISTICOS.contains(selectedRole)) {
                        tvZonaLabel.visibility = View.VISIBLE
                        spZona.visibility = View.VISIBLE

                        // Mostrar campos de Cliente (Nombre/Teléfono)
                        // 🏆 Cambio: Mantenemos Nombre/Teléfono VISIBLES
                        // Los datos se usan para ambos tipos de registro ahora.
                        tilFullName.visibility = View.VISIBLE
                        tilPhoneNumber.visibility = View.VISIBLE

                        // Validamos que el administrador ingrese el Nombre completo del empleado
                        tilFullName.hint = getString(R.string.full_name_logistic_hint)
                        tilPhoneNumber.hint = getString(R.string.phone_number_logistic_hint)

                    } else if (selectedRole == ROL_CLIENTE) {
                        // Ocultar campos de Zona
                        tvZonaLabel.visibility = View.GONE
                        spZona.visibility = View.GONE

                        // Mostrar campos de Cliente
                        tilFullName.visibility = View.VISIBLE
                        tilPhoneNumber.visibility = View.VISIBLE

                        // Restaurar hints
                        tilFullName.hint = getString(R.string.full_name_client_hint)
                        tilPhoneNumber.hint = getString(R.string.phone_number_client_hint)
                    } else {
                        // Caso por defecto (Admin, si fuera seleccionable)
                        tvZonaLabel.visibility = View.GONE
                        spZona.visibility = View.GONE
                        tilFullName.visibility = View.VISIBLE
                        tilPhoneNumber.visibility = View.VISIBLE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupListeners() {
        btnGoRegister.setOnClickListener {
            performRegistration()
        }

        findViewById<MaterialButton>(R.id.btnGoLogin).setOnClickListener {
            finish()
        }
    }

    /**
     * Realiza la validación de campos y el intento de registro.
     */
    private fun performRegistration() {
        // Limpiar errores
        tilFullName.error = null
        tilPhoneNumber.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilPassword2.error = null

        // OBTENER VALORES
        val fullName = etFullName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val password2 = etPassword2.text.toString()

        val role = if (isAdminRegister) spRol.selectedItem.toString().uppercase() else ROL_CLIENTE
        val zona = if (spZona.visibility == View.VISIBLE) spZona.selectedItem.toString() else null

        // 1. VALIDACIÓN DE CAMPOS GENERALES
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Debe llenar los campos de Email y Contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🏆 Validación de Nombre/Teléfono para AMBOS roles (Cliente y Logístico)
        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Nombre y Teléfono son obligatorios para el registro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (phoneNumber.length < 7) {
            tilPhoneNumber.error = "Número de teléfono incompleto."
            return
        }

        // 2. VALIDACIÓN DE FORMATO
        if (!isValidEmail(email)) {
            tilEmail.error = "Formato de email inválido."
            return
        }

        // 3. VALIDACIÓN DE CONTRASEÑA REFORZADA
        val passwordValidationResult = isValidPassword(password)
        if (passwordValidationResult != null) {
            tilPassword.error = passwordValidationResult
            return
        }

        if (password != password2) {
            tilPassword2.error = "Las contraseñas no coinciden."
            return
        }

        // 4. VALIDACIÓN CONDICIONAL DE ROL/ZONA
        if (ROLES_LOGISTICOS.contains(role) && zona.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Debe seleccionar una zona para el rol ${role}.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 5. HASH DE CONTRASEÑA
        val passwordHash = PasswordHasher.hashPassword(password) // 🏆 Usando PasswordHasher

        // 6. INTENTO DE REGISTRO
        setLoadingState(true)

        val newId: Long = when (role) {
            ROL_CLIENTE -> {
                // Registrar Cliente: Usa Nombre y Teléfono
                userRepository.registerClient(
                    email = email,
                    passwordHash = passwordHash,
                    fullName = fullName,
                    phoneNumber = phoneNumber
                )
            }

            else -> {
                // Registrar Personal Logístico: Usa Nombre, Teléfono, Rol y Zona
                userRepository.registerRecolector(
                    name = fullName, // 🏆 CORRECCIÓN CLAVE: Usar fullName, no email
                    email = email,
                    passwordHash = passwordHash,
                    role = role,
                    sucursal = zona
                )
            }
        }

        setLoadingState(false)

        if (newId != -1L) {
            Toast.makeText(
                this,
                "Registro Exitoso como $role! Por favor, inicia sesión.",
                Toast.LENGTH_LONG
            ).show()

            // Redirección
            val nextIntent = if (isAdminRegister) {
                // Si el administrador registra, permanece en la pantalla actual para seguir registrando.
                null
            } else {
                // Si es registro público, va a Login
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            }

            if (nextIntent != null) {
                startActivity(nextIntent)
                finish()
            } else {
                // Limpiar campos después de registrar si es el flujo Admin
                etFullName.text?.clear()
                etPhoneNumber.text?.clear()
                etEmail.text?.clear()
                etPassword.text?.clear()
                etPassword2.text?.clear()
            }

        } else {
            Toast.makeText(
                this,
                "Error: El email ya está registrado o falló la base de datos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGoRegister.isEnabled = !isLoading
        findViewById<MaterialButton>(R.id.btnGoLogin)?.isEnabled = !isLoading

        // Deshabilitar/Habilitar los campos de entrada
        etFullName.isEnabled = !isLoading
        etPhoneNumber.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etPassword2.isEnabled = !isLoading
        spRol.isEnabled = !isLoading
        spZona.isEnabled = !isLoading
    }

    // --- FUNCIONES DE SEGURIDAD Y VALIDACIÓN ---

    private fun isValidPassword(password: String): String? {
        if (password.length < 8) return "La contraseña debe tener al menos 8 caracteres."
        if (!password.matches(".*[A-Z].*".toRegex())) return "Debe contener al menos una letra mayúscula."
        if (!password.matches(".*[a-z].*".toRegex())) return "Debe contener al menos una letra minúscula."
        if (!password.matches(".*[0-9].*".toRegex())) return "Debe contener al menos un número."

        val normalizedPassword = password.lowercase()
        if (PASSWORD_BLACKLIST.any { normalizedPassword.contains(it) }) {
            return "La contraseña es muy común. Por favor, usa una más compleja."
        }
        return null
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    // 🏆 Eliminada la función hashPassword local para usar co.edu.unipiloto.myapplication.security.PasswordHasher
}