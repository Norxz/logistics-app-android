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
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.security.MessageDigest

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
    private lateinit var etFullName: TextInputEditText      // NUEVO: Nombre
    private lateinit var etPhoneNumber: TextInputEditText   // NUEVO: Teléfono
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPassword2: TextInputEditText

    private lateinit var tilFullName: TextInputLayout       // NUEVO: Nombre
    private lateinit var tilPhoneNumber: TextInputLayout    // NUEVO: Teléfono
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
        // 🏆 INICIALIZACIÓN DE NUEVOS CAMPOS DE NOMBRE Y TELÉFONO
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

            // Ocultar campos de Nombre/Teléfono si el rol NO es cliente (esto es una suposición)
            // Si el registro público ES para clientes, ESTOS DEBEN ESTAR VISIBLES.
            // Los mantendremos visibles por defecto para el registro público.

        } else {
            // Flujo Admin
            // Ocultar Nombre/Teléfono si el rol NO es cliente (se actualizará en el listener del Spinner)
            // Por defecto, se muestran todos los campos en el flujo Admin y se ocultan los de cliente/logística según el rol.
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

                        // Ocultar campos de Cliente (Nombre/Teléfono)
                        tilFullName.visibility = View.GONE
                        tilPhoneNumber.visibility = View.GONE
                    } else if (selectedRole == ROL_CLIENTE) {
                        // Mostrar campos de Cliente
                        tilFullName.visibility = View.VISIBLE
                        tilPhoneNumber.visibility = View.VISIBLE

                        // Ocultar campos de Zona
                        tvZonaLabel.visibility = View.GONE
                        spZona.visibility = View.GONE
                    } else {
                        // Caso por defecto (puede que no se necesite si ADMIN_REGISTERABLE_ROLES es estricto)
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
        // Limpiar errores (AÑADIDOS NUEVOS CAMPOS)
        tilFullName.error = null
        tilPhoneNumber.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilPassword2.error = null

        // OBTENER NUEVOS VALORES
        val fullName = etFullName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val password2 = etPassword2.text.toString()

        val role = if (isAdminRegister) spRol.selectedItem.toString().uppercase() else ROL_CLIENTE
        val zona = if (spZona.visibility == View.VISIBLE) spZona.selectedItem.toString() else null

        // 1. VALIDACIÓN DE CAMPOS SEGÚN EL ROL
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Debe llenar todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🏆 Validación específica del CLIENTE
        if (role == ROL_CLIENTE) {
            if (fullName.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Nombre y Teléfono son obligatorios para Clientes.", Toast.LENGTH_SHORT).show()
                return
            }
            if (phoneNumber.length < 7) {
                tilPhoneNumber.error = "Número de teléfono incompleto."
                return
            }
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
        val passwordHash = hashPassword(password)

        // 6. INTENTO DE REGISTRO
        setLoadingState(true)

        val newId: Long = when (role) {
            ROL_CLIENTE -> {
                // Registrar Cliente: Usando Nombre y Teléfono reales
                userRepository.registerClient(
                    email = email,
                    passwordHash = passwordHash,
                    fullName = fullName, // 🏆 USANDO CAMPO REAL
                    phoneNumber = phoneNumber // 🏆 USANDO CAMPO REAL
                )
            }

            else -> {
                // Registrar Personal Logístico
                userRepository.registerRecolector(
                    username = email,
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

            // Redirección a Login (o al Dashboard si viene de Admin)
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
                etFullName.text?.clear() // Limpiar nuevo campo
                etPhoneNumber.text?.clear() // Limpiar nuevo campo
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

        // Deshabilitar/Habilitar los nuevos campos
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

    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("Security", "Error hashing password: ${e.message}")
            password
        }
    }
}