package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
// ❌ ELIMINAR: import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.model.LogisticUser // Modelo de datos
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 NUEVO: Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditLogisticUserActivity : AppCompatActivity() {

    // ❌ ELIMINADA: private lateinit var userRepository: UserRepository
    private var recolectorId: Long = -1L
    private var currentUser: LogisticUser? = null

    // Vistas (se mantienen)
    private lateinit var tvTitle: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerSucursal: Spinner
    private lateinit var switchActive: Switch
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageButton

    // Opciones (se mantienen)
    private val roles = arrayOf("CONDUCTOR", "FUNCIONARIO", "GESTOR")
    private val sucursales = arrayOf("Bogotá - Norte", "Bogotá - Sur", "Medellín", "Cali", "Barranquilla", "N/A")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_logistic_user)
        supportActionBar?.hide()


        recolectorId = intent.getLongExtra("RECOLECTOR_ID", -1L)
        if (recolectorId == -1L) {
            Toast.makeText(this, "Error: ID de usuario no proporcionado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadUserData() // Ahora asíncrono

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

    /**
     * Carga los datos del usuario desde el backend REST.
     */
    private fun loadUserData() {
        RetrofitClient.apiService.getLogisticUserById(recolectorId).enqueue(object : Callback<LogisticUser> {
            override fun onResponse(call: Call<LogisticUser>, response: Response<LogisticUser>) {
                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()

                    // 🏆 FLUJO ASÍNCRONO CORRECTO: Una vez que currentUser está cargado:
                    displayUserData(currentUser!!)
                    setupSpinners(currentUser!!) // Inicializa Spinners con data
                    setupListeners() // Habilita botones de guardar

                } else {
                    Toast.makeText(this@EditLogisticUserActivity, "Usuario no encontrado en el servidor.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<LogisticUser>, t: Throwable) {
                Toast.makeText(this@EditLogisticUserActivity, "Fallo de red al cargar usuario.", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }

    /**
     * Muestra los datos obtenidos del backend en la UI.
     */
    private fun displayUserData(user: LogisticUser) {
        tvTitle.text = "Editar: ${user.name}"
        etName.setText(user.name)
        etEmail.setText(user.email)
        etPhone.setText(user.phoneNumber)
        switchActive.isChecked = user.isActive

        updateSwitchText(user.isActive)
        setupSpinners(user) // Llamamos setupSpinners con el usuario cargado
    }

    private fun updateSwitchText(isActive: Boolean) {
        switchActive.text = if (isActive) "Estado: Activo" else "Estado: Inactivo"
    }

    private fun setupSpinners(user: LogisticUser) { // Acepta el usuario cargado
        // Adaptador de Roles
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = roleAdapter
        val currentRoleIndex = roles.indexOf(user.role)
        if (currentRoleIndex != -1) spinnerRole.setSelection(currentRoleIndex)

        // Adaptador de Sucursales
        val sucursalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sucursales)
        spinnerSucursal.adapter = sucursalAdapter
        val currentSucursalIndex = sucursales.indexOf(user.sucursal)
        if (currentSucursalIndex != -1) spinnerSucursal.setSelection(currentSucursalIndex)
    }

    private fun setupListeners() {
        // ... (Listeners para Back y Switch se mantienen) ...

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchText(isChecked)
        }

        btnSave.setOnClickListener {
            saveChanges() // Ahora llama a la lógica REST
        }
    }

    /**
     * Guarda los cambios llamando al endpoint REST (PUT).
     */
    private fun saveChanges() {
        val user = currentUser ?: return

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

        // 1. Crear la copia del usuario con los datos actualizados
        val updatedUser = user.copy(
            name = newName,
            email = newEmail,
            phoneNumber = newPhone,
            role = newRole,
            sucursal = newSucursal,
            isActive = newIsActive
        )

        // 2. 🏆 LLAMADA A RETROFIT (PUT)
        RetrofitClient.apiService.updateLogisticUser(updatedUser.id, updatedUser).enqueue(object : Callback<LogisticUser> {
            override fun onResponse(call: Call<LogisticUser>, response: Response<LogisticUser>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditLogisticUserActivity, "Usuario ${newName} actualizado con éxito.", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK) // Notificar a la Activity anterior para refrescar
                    finish()
                } else {
                    Toast.makeText(this@EditLogisticUserActivity, "Error al guardar los cambios: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LogisticUser>, t: Throwable) {
                Toast.makeText(this@EditLogisticUserActivity, "Fallo de red al actualizar usuario.", Toast.LENGTH_LONG).show()
            }
        })
    }
}