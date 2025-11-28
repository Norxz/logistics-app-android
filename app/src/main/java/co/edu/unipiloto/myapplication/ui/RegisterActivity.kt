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
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.RegisterRequest
import co.edu.unipiloto.myapplication.model.User
import co.edu.unipiloto.myapplication.model.Sucursal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity para el registro de nuevos usuarios.
 */
class RegisterActivity : AppCompatActivity() {

    // --- CONSTANTES ---
    companion object {
        const val EXTRA_IS_ADMIN_REGISTER = "IS_ADMIN_REGISTER"
        const val ROL_CLIENTE = "CLIENTE"
        const val ROL_ADMIN = "ADMIN"
    }

    // --- VISTAS (se mantienen) ---
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
    private lateinit var sessionManager: SessionManager

    // Roles que requieren selección de Zona
    private val ROLES_LOGISTICOS = listOf("CONDUCTOR", "GESTOR", "FUNCIONARIO", "ANALISTA")
    private val ADMIN_REGISTERABLE_ROLES = listOf(ROL_CLIENTE) + ROLES_LOGISTICOS.distinct()
    private lateinit var sucursalesList: List<Sucursal>
    private val PASSWORD_BLACKLIST =
        listOf("password", "123456", "qwerty", "admin", "unipiloto", "piloto")

    // --- ESTADO ---
    private var isAdminRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        isAdminRegister = intent.getBooleanExtra(EXTRA_IS_ADMIN_REGISTER, false)

        initViews()
        setupSpinners()
        setupRegistrationFlowUI()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        tilFullName = findViewById(R.id.tilFullName)
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPassword2 = findViewById(R.id.etPassword2)
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
            tvRolLabel.visibility = View.GONE
            spRol.visibility = View.GONE
            tvZonaLabel.visibility = View.GONE
            spZona.visibility = View.GONE
        } else {
            val initialRole = ADMIN_REGISTERABLE_ROLES.firstOrNull() ?: ROL_CLIENTE
            val isLogistic = ROLES_LOGISTICOS.contains(initialRole)
            tvZonaLabel.visibility = if (isLogistic) View.VISIBLE else View.GONE
            spZona.visibility = if (isLogistic) View.VISIBLE else View.GONE
            tilFullName.visibility = View.VISIBLE // Mantenemos visible para logísticos también
            tilPhoneNumber.visibility = View.VISIBLE // Mantenemos visible
        }
    }

    private fun setupSpinners() {
        val rolesToShow = if (isAdminRegister) ADMIN_REGISTERABLE_ROLES else listOf(ROL_CLIENTE)
        val rolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rolesToShow)
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRol.adapter = rolAdapter

        val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListOf<String>())
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spZona.adapter = emptyAdapter

        loadSucursalesFromServer()

        if (isAdminRegister) {
            spRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedRole = parent?.getItemAtPosition(position).toString().uppercase()
                    val isLogistic = ROLES_LOGISTICOS.contains(selectedRole)

                    tvZonaLabel.visibility = if (isLogistic) View.VISIBLE else View.GONE
                    spZona.visibility = if (isLogistic) View.VISIBLE else View.GONE

                    val hintResource = if (isLogistic) R.string.full_name_logistic_hint else R.string.full_name_client_hint
                    val phoneHintResource = if (isLogistic) R.string.phone_number_logistic_hint else R.string.phone_number_client_hint

                    tilFullName.hint = getString(hintResource)
                    tilPhoneNumber.hint = getString(phoneHintResource)
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
     * Realiza la validación de campos y el intento de registro REST.
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

        // 1. VALIDACIÓN
        if (!validateFieldsAndPassword(fullName, phoneNumber, email, password, password2, role, zona)) return

        val sucursalId = if (spZona.visibility == View.VISIBLE) {
            sucursalesList[spZona.selectedItemPosition].id
        } else null

        // 2. 🌟 CREAR REQUEST DTO para el Backend 🌟
        val registerRequest = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            role = role,
            sucursalId = sucursalId,  // ✔ AHORA SÍ COINCIDE CON EL BACKEND
            isActive = true
        )

        // 3. INTENTO DE REGISTRO REST
        setLoadingState(true)

        RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                setLoadingState(false)

                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registro Exitoso como ${userData.role}!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Redirección
                    handleSuccessfulRegistration(isAdminRegister)

                } else {
                    // Manejo de errores 409 (Conflict) u otros errores del servidor
                    val errorBody = response.errorBody()?.string()

                    if (response.code() == 409) { // 409 CONFLICT: Email ya registrado
                        tilEmail.error = "El email ya está registrado."
                        Toast.makeText(this@RegisterActivity, "Error: Email ya registrado.", Toast.LENGTH_LONG).show()
                    } else {
                        Log.e("Register", "Error ${response.code()}: $errorBody")
                        Toast.makeText(this@RegisterActivity, "Error al registrar. Intente de nuevo.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                setLoadingState(false)
                Log.e("Register", "Fallo de red: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Fallo de red. Verifique el servidor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    // 🏆 Nuevo método que centraliza la validación (ayuda a la limpieza)
    private fun validateFieldsAndPassword(
        fullName: String, phoneNumber: String, email: String,
        password: String, password2: String, role: String, zona: String?
    ): Boolean {
        // VALIDACIÓN DE CAMPOS GENERALES
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Debe llenar todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phoneNumber.length < 7) { tilPhoneNumber.error = "Número de teléfono incompleto."; return false }
        if (!isValidEmail(email)) { tilEmail.error = "Formato de email inválido."; return false }

        // VALIDACIÓN DE CONTRASEÑA
        val passwordValidationResult = isValidPassword(password)
        if (passwordValidationResult != null) { tilPassword.error = passwordValidationResult; return false }
        if (password != password2) { tilPassword2.error = "Las contraseñas no coinciden."; return false }

        // VALIDACIÓN CONDICIONAL DE ROL/ZONA
        if (ROLES_LOGISTICOS.contains(role) && zona.isNullOrEmpty()) {
            Toast.makeText(this, "Debe seleccionar una zona para el rol ${role}.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun handleSuccessfulRegistration(isAdminRegister: Boolean) {
        if (!isAdminRegister) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        } else {
            // Limpiar campos después de registrar si es el flujo Admin
            etFullName.text?.clear()
            etPhoneNumber.text?.clear()
            etEmail.text?.clear()
            etPassword.text?.clear()
            etPassword2.text?.clear()
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGoRegister.isEnabled = !isLoading
        findViewById<MaterialButton>(R.id.btnGoLogin)?.isEnabled = !isLoading

        etFullName.isEnabled = !isLoading
        etPhoneNumber.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etPassword2.isEnabled = !isLoading
        spRol.isEnabled = !isLoading
        spZona.isEnabled = !isLoading
    }

    // --- FUNCIONES DE SEGURIDAD Y VALIDACIÓN (Mantenidas) ---

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

    private fun loadSucursalesFromServer() {
        RetrofitClient.apiService.getSucursales().enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(
                call: Call<List<Sucursal>>,
                response: Response<List<Sucursal>>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    sucursalesList = response.body()!!

                    // Construimos "Ciudad - Nombre"
                    val sucursales = sucursalesList.map { s ->
                        "${s.direccion?.ciudad ?: "Ciudad desconocida"} - ${s.nombre}"
                    }

                    val adapter = ArrayAdapter(
                        this@RegisterActivity,
                        android.R.layout.simple_spinner_item,
                        sucursales
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spZona.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Error al cargar sucursales", Toast.LENGTH_SHORT).show()
            }
        })
    }
}