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

    // DICCIONARIO BÁSICO DE PALABRAS PROHIBIDAS (Lista Negra)
    private val PASSWORD_BLACKLIST = listOf("password", "123456", "qwerty", "admin", "unipiloto", "piloto")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Ocultar la barra de acción para usar el diseño personalizado del layout
        supportActionBar?.hide()

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

        // Manejar el caso donde el parent.parent podría ser null o incorrecto
        tilEmail = etEmail.parent.parent as? TextInputLayout ?: findViewById(R.id.tilEmail)
        tilPassword = etPassword.parent.parent as? TextInputLayout ?: findViewById(R.id.tilPassword)
        tilPassword2 = etPassword2.parent.parent as? TextInputLayout ?: findViewById(R.id.tilPassword2)

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
        val roles = listOf("CLIENTE", "CONDUCTOR", "GESTOR", "FUNCIONARIO", "ANALISTA")
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

        // 1. VALIDACIÓN BÁSICA DE CAMPOS VACÍOS
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Debe llenar todos los campos.", Toast.LENGTH_SHORT).show()
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
        val passwordHash = hashPassword(password)

        // 6. INTENTO DE REGISTRO
        progressBar.visibility = View.VISIBLE

        val newId: Long = when (role) {
            "CLIENTE" -> {
                // Registro de CLIENTE
                userRepository.registerClient(
                    email = email,
                    passwordHash = passwordHash,
                    fullName = "Cliente ${email.split("@")[0]}", // Nombre simple basado en email
                    phoneNumber = "0" // Placeholder
                )
            }

            else -> {
                // Registro de Personal Logístico (CONDUCTOR, GESTOR, etc.)
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
            Toast.makeText(this, "Registro Exitoso como $role! Por favor, inicia sesión.", Toast.LENGTH_LONG).show()

            // 7. 🏆 REDIRECCIÓN A LOGIN EN LUGAR DE INICIO DE SESIÓN AUTOMÁTICO
            val intent = Intent(this, LoginActivity::class.java)
            // Estas flags aseguran que no se pueda volver a RegisterActivity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(
                this,
                "Error: El email ya está registrado o falló la base de datos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // --- FUNCIONES DE SEGURIDAD Y VALIDACIÓN ---

    /**
     * Aplica políticas de seguridad a la contraseña (mín 8, mayús, minús, número, lista negra).
     * @return String con mensaje de error, o null si es válida.
     */
    private fun isValidPassword(password: String): String? {
        if (password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres."
        }
        // Requiere al menos una mayúscula, una minúscula y un dígito.
        if (!password.matches(".*[A-Z].*".toRegex())) {
            return "Debe contener al menos una letra mayúscula."
        }
        if (!password.matches(".*[a-z].*".toRegex())) {
            return "Debe contener al menos una letra minúscula."
        }
        if (!password.matches(".*[0-9].*".toRegex())) {
            return "Debe contener al menos un número."
        }

        // Validación con diccionario (Lista Negra)
        val normalizedPassword = password.lowercase()
        if (PASSWORD_BLACKLIST.any { normalizedPassword.contains(it) }) {
            return "La contraseña es muy común. Por favor, usa una más compleja."
        }

        return null // Contraseña es válida
    }

    /**
     * Valida el formato de correo electrónico.
     */
    private fun isValidEmail(target: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    /**
     * Genera un hash SHA-256 de la contraseña para almacenamiento seguro.
     */
    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("Security", "Error hashing password: ${e.message}")
            password // Retorno simple si el hashing falla (Peligroso)
        }
    }
}