package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.LogisticUser
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditLogisticUserActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private var recolectorId: Long = -1L
    private var currentUser: LogisticUser? = null

    // Vistas
    private lateinit var tvTitle: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerSucursal: Spinner
    private lateinit var switchActive: Switch
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageButton

    // Opciones
    private val roles = arrayOf("CONDUCTOR", "FUNCIONARIO", "GESTOR") // Roles disponibles
    private val sucursales = arrayOf("Bogotá - Norte", "Bogotá - Sur", "Medellín", "Cali", "Barranquilla", "N/A") // Sucursales

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_logistic_user)
        supportActionBar?.hide()

        userRepository = UserRepository(this)

        // 1. Obtener el ID del usuario
        recolectorId = intent.getLongExtra("RECOLECTOR_ID", -1L)
        if (recolectorId == -1L) {
            Toast.makeText(this, "Error: ID de usuario no proporcionado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadUserData()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvEditTitle)
        etName = findViewById(R.id.etEditName)
        etEmail = findViewById(R.id.etEditEmail)
        etPhone = findViewById(R.id.etEditPhone)
        spinnerRole = findViewById(R.id.spinnerEditRole)
        spinnerSucursal = findViewById(R.id.spinnerEditSucursal)
        switchActive = findViewById(R.id.switchIsActive)
        btnSave = findViewById(R.id.btnSaveUser)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadUserData() {
        // Cargar usuario desde la BD
        currentUser = userRepository.getLogisticUserById(recolectorId)

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Mostrar datos en la UI
        tvTitle.text = "Editar: ${currentUser!!.name}"
        etName.setText(currentUser!!.name)
        etEmail.setText(currentUser!!.email)
        etPhone.setText(currentUser!!.phoneNumber)
        switchActive.isChecked = currentUser!!.isActive

        // Inicializar Switch con texto basado en el estado
        updateSwitchText(currentUser!!.isActive)

        // El Spinner se inicializa después en setupSpinners()
    }

    private fun updateSwitchText(isActive: Boolean) {
        switchActive.text = if (isActive) "Estado: Activo" else "Estado: Inactivo"
    }

    private fun setupSpinners() {
        // Adaptador de Roles
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = roleAdapter
        val currentRoleIndex = roles.indexOf(currentUser!!.role)
        if (currentRoleIndex != -1) spinnerRole.setSelection(currentRoleIndex)

        // Adaptador de Sucursales
        val sucursalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sucursales)
        spinnerSucursal.adapter = sucursalAdapter
        val currentSucursalIndex = sucursales.indexOf(currentUser!!.sucursal)
        if (currentSucursalIndex != -1) spinnerSucursal.setSelection(currentSucursalIndex)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchText(isChecked)
        }

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val newName = etName.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()
        val newRole = spinnerRole.selectedItem.toString()
        val newSucursal = spinnerSucursal.selectedItem.toString()
        val newIsActive = switchActive.isChecked

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "El nombre y el email son obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear una copia del usuario con los nuevos datos
        val updatedUser = currentUser!!.copy(
            name = newName,
            email = newEmail,
            phoneNumber = newPhone,
            role = newRole,
            sucursal = newSucursal,
            isActive = newIsActive
        )

        // Guardar en la BD
        val success = userRepository.updateLogisticUser(updatedUser)

        if (success) {
            Toast.makeText(this, "Usuario ${newName} actualizado con éxito.", Toast.LENGTH_LONG).show()
            // Notificar a la Activity anterior (ViewLogisticUsersActivity) que debe refrescar
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error al guardar los cambios.", Toast.LENGTH_LONG).show()
        }
    }
}