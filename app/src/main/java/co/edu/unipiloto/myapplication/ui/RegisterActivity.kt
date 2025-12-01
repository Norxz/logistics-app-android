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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import co.edu.unipiloto.myapplication.api.SucursalApi
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import co.edu.unipiloto.myapplication.dto.RetrofitClient // 💡 Importación del objeto RetrofitClient
import co.edu.unipiloto.myapplication.dto.RegisterRequest
import co.edu.unipiloto.myapplication.model.User
import co.edu.unipiloto.myapplication.model.Sucursal
// Importamos la clase SucursalResponse para el mapeo
import co.edu.unipiloto.myapplication.dto.SucursalResponse // 👈 ASUMIDO: Necesitas importar esta clase DTO

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
    private lateinit var spSucursal: Spinner
    private lateinit var tvRolLabel: TextView
    private lateinit var tvSucursalLabel: TextView
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var progressBar: ProgressBar

    // --- DATOS Y UTILIDADES ---
    private lateinit var sessionManager: SessionManager

    // Roles que requieren selección de Sucursal (anteriormente Zona)
    private val ROLES_LOGISTICOS = listOf("CONDUCTOR", "GESTOR", "FUNCIONARIO", "ANALISTA")
    private val ADMIN_REGISTERABLE_ROLES = listOf(ROL_CLIENTE) + ROLES_LOGISTICOS.distinct()

    // 💡 Inicialización segura para la lista de sucursales
    private var sucursalesList: List<Sucursal> = emptyList()

    private val PASSWORD_BLACKLIST =
        listOf("password", "123456", "qwerty", "admin", "unipiloto", "piloto")

    // --- ESTADO ---
    private var isAdminRegister = false

    // 💡 Propiedad computada para obtener el rol seleccionado (del fragmento anterior)
    private val selectedRole: String
        get() {
            return if (isAdminRegister) {
                spRol.selectedItem.toString().uppercase()
            } else {
                ROL_CLIENTE
            }
        }

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
        spSucursal = findViewById(R.id.spZona)       // 👈 Mantiene el ID del XML, usa nuevo nombre
        tvRolLabel = findViewById(R.id.tvRolLabel)
        tvSucursalLabel = findViewById(R.id.tvZonaLabel) // 👈 Mantiene el ID del XML, usa nuevo nombre
        btnGoRegister = findViewById(R.id.btnGoRegister)
        progressBar = findViewById(R.id.progress)
    }

    private fun setupRegistrationFlowUI() {
        if (!isAdminRegister) {
            tvRolLabel.visibility = View.GONE
            spRol.visibility = View.GONE
            tvSucursalLabel.visibility = View.GONE // 👈 Actualizado
            spSucursal.visibility = View.GONE      // 👈 Actualizado
        } else {
            val initialRole = ADMIN_REGISTERABLE_ROLES.firstOrNull() ?: ROL_CLIENTE
            val isLogistic = ROLES_LOGISTICOS.contains(initialRole)

            tvSucursalLabel.visibility = if (isLogistic) View.VISIBLE else View.GONE // 👈 Actualizado
            spSucursal.visibility = if (isLogistic) View.VISIBLE else View.GONE      // 👈 Actualizado

            tilFullName.visibility = View.VISIBLE
            tilPhoneNumber.visibility = View.VISIBLE
        }
    }

    private fun setupSpinners() {
        val rolesToShow = if (isAdminRegister) ADMIN_REGISTERABLE_ROLES else listOf(ROL_CLIENTE)
        val rolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rolesToShow)
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRol.adapter = rolAdapter

        // Usamos spSucursal
        val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListOf("Cargando Sucursales..."))
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSucursal.adapter = emptyAdapter // 👈 Actualizado

        loadSucursalesFromServer()

        if (isAdminRegister) {
            spRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val role = parent?.getItemAtPosition(position).toString().uppercase()
                    val isLogistic = ROLES_LOGISTICOS.contains(role)

                    tvSucursalLabel.visibility = if (isLogistic) View.VISIBLE else View.GONE // 👈 Actualizado
                    spSucursal.visibility = if (isLogistic) View.VISIBLE else View.GONE      // 👈 Actualizado

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

        val role = selectedRole // Usa la propiedad computada

        // 💡 Obtenemos el nombre de la sucursal (anteriormente zona)
        val sucursalName = if (spSucursal.visibility == View.VISIBLE && spSucursal.selectedItem != null) {
            spSucursal.selectedItem.toString()
        } else null

        // 1. VALIDACIÓN
        if (!validateFieldsAndPassword(fullName, phoneNumber, email, password, password2, role, sucursalName)) return // 👈 sucursalName

        // 💡 CORRECCIÓN CRÍTICA: Obtener el ID de la sucursal de forma segura
        val sucursalId = if (ROLES_LOGISTICOS.contains(role)) {
            if (sucursalesList.isNotEmpty() && spSucursal.selectedItemPosition >= 0) {
                sucursalesList[spSucursal.selectedItemPosition].id // 👈 spSucursal
            } else {
                Toast.makeText(this, "Error: Debe seleccionar una sucursal válida.", Toast.LENGTH_LONG).show() // 👈 Texto actualizado
                return
            }
        } else null

        // 2. 🌟 CREAR REQUEST DTO para el Backend 🌟
        val registerRequest = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            role = role,
            sucursalId = sucursalId,
            isActive = true
        )

        // 3. INTENTO DE REGISTRO REST USANDO CORRUTINAS (Suspend function)
        setLoadingState(true)

        lifecycleScope.launch { // 👈 Inicia una corrutina en el scope de la Activity
            try {
                // 🎯 CORRECCIÓN: Usar la función getter RetrofitClient.getAuthApi()
                val response = RetrofitClient.getAuthApi().register(registerRequest)

                setLoadingState(false)

                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registro Exitoso como ${userData.role}!",
                        Toast.LENGTH_LONG
                    ).show()

                    handleSuccessfulRegistration(isAdminRegister)

                } else {
                    // Manejo de errores
                    val errorBody = response.errorBody()?.string()

                    if (response.code() == 409) { // 409 CONFLICT: Email ya registrado
                        tilEmail.error = "El email ya está registrado."
                        Toast.makeText(this@RegisterActivity, "Error: Email ya registrado.", Toast.LENGTH_LONG).show()
                    } else {
                        Log.e("Register", "Error ${response.code()}: $errorBody")
                        Toast.makeText(this@RegisterActivity, "Error al registrar. Intente de nuevo.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                setLoadingState(false)
                Log.e("Register", "Fallo de red/excepción: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Fallo de red. Verifique el servidor.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 🏆 Nuevo método que centraliza la validación (ayuda a la limpieza)
    private fun validateFieldsAndPassword(
        fullName: String, phoneNumber: String, email: String,
        password: String, password2: String, role: String, sucursalName: String? // 👈 sucursalName
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

        // VALIDACIÓN CONDICIONAL DE ROL/SUCURSAL
        if (ROLES_LOGISTICOS.contains(role)) {
            if (sucursalName.isNullOrEmpty()) { // 👈 sucursalName
                Toast.makeText(this, "Debe seleccionar una sucursal para el rol ${role}.", Toast.LENGTH_SHORT).show() // 👈 Texto actualizado
                return false
            }
            // Aunque sucursalesList.isEmpty() se maneja en loadSucursalesFromServer,
            // si la lista está vacía aquí, el index puede fallar.
            if (sucursalesList.isEmpty()) {
                Toast.makeText(this, "Las sucursales no han cargado correctamente. Intente de nuevo.", Toast.LENGTH_LONG).show()
                return false
            }
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
        spSucursal.isEnabled = !isLoading // 👈 Actualizado
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
        lifecycleScope.launch {
            try {
                // 🎯 CORRECCIÓN: Usar la función getter RetrofitClient.getSucursalApi()
                val response = RetrofitClient.getSucursalApi().listarSucursales()

                if (response.isSuccessful && response.body() != null) {

                    // response.body() es List<SucursalResponse>
                    sucursalesList = response.body()!!.map { sucursalDto ->
                        Sucursal(
                            sucursalDto.id,
                            sucursalDto.nombre,
                            sucursalDto.direccion
                        )
                    }

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
                    spSucursal.adapter = adapter
                } else {
                    Log.e("Sucursal", "Error al cargar: ${response.code()}")
                    Toast.makeText(this@RegisterActivity, "Error al cargar sucursales", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Sucursal", "Fallo de red/excepción: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Fallo de red al cargar sucursales", Toast.LENGTH_SHORT).show()
            }
        }
    }
}