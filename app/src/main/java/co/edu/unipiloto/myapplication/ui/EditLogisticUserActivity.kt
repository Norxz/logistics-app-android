package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
// ❌ ELIMINAR: import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.model.Sucursal
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 NUEVO: Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditLogisticUserActivity : AppCompatActivity() {

    // ❌ ELIMINADA: private lateinit var userRepository: UserRepository
    private var recolectorId: Long = -1L
    private var currentUser: User? = null

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
    private var sucursales: List<Sucursal> = emptyList()

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
        loadUserData()
        loadSucursales()
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
        RetrofitClient.apiService.getLogisticUserById(recolectorId)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        currentUser = response.body()
                        displayUserData()
                        if (sucursales.isNotEmpty()) setupSpinners()
                        setupListeners()
                    } else {
                        Toast.makeText(this@EditLogisticUserActivity, "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@EditLogisticUserActivity, "Fallo de red al cargar usuario.", Toast.LENGTH_LONG).show()
                    finish()
                }
            })
    }

    /**
     * Carga los datos del usuario desde el backend REST.
     */
    private fun loadSucursales() {
        RetrofitClient.apiService.getAllSucursales()
            .enqueue(object : Callback<List<Sucursal>> {
                override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
                    if (response.isSuccessful && response.body() != null) {
                        sucursales = response.body()!!
                        if (currentUser != null) setupSpinners()
                    }
                }

                override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                    Toast.makeText(this@EditLogisticUserActivity, "Error cargando sucursales.", Toast.LENGTH_LONG).show()
                }
            })
    }

    /**
     * Muestra los datos obtenidos del backend en la UI.
     */
    private fun displayUserData() {
        val user = currentUser ?: return

        tvTitle.text = "Editar: ${user.fullName}"
        etName.setText(user.fullName)
        etEmail.setText(user.email)
        etPhone.setText(user.phoneNumber)
        switchActive.isChecked = user.isActive
        updateSwitchText(user.isActive)
    }

    private fun updateSwitchText(isActive: Boolean) {
        switchActive.text = if (isActive) "Estado: Activo" else "Estado: Inactivo"
    }

    private fun setupSpinners() {
        val user = currentUser ?: return

        // -------- ROLES --------
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = roleAdapter
        spinnerRole.setSelection(roles.indexOf(user.role))

        // -------- SUCURSALES --------
        val sucursalNames = sucursales.map { it.nombre }
        val sucursalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sucursalNames)
        spinnerSucursal.adapter = sucursalAdapter

        val indexFound = sucursales.indexOfFirst { it.id == user.sucursal?.id }
        val safeIndex = if (indexFound != -1) indexFound else 0

        spinnerSucursal.setSelection(safeIndex)
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
        val newSucursal = sucursales[spinnerSucursal.selectedItemPosition]
        val newIsActive = switchActive.isChecked

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Nombre y email obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = user.copy(
            fullName = newName,
            email = newEmail,
            phoneNumber = newPhone,
            role = newRole,
            sucursal = newSucursal,     // <--- ✔ ENVÍO OBJETO COMPLETO
            isActive = newIsActive
        )

        RetrofitClient.apiService.updateLogisticUser(updatedUser.id, updatedUser)
            .enqueue(object : Callback<User> {
                override fun onResponse(
                    call: Call<User>,
                    response: Response<User>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditLogisticUserActivity, "Actualizado con éxito.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditLogisticUserActivity, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@EditLogisticUserActivity, "Fallo de red.", Toast.LENGTH_LONG).show()
                }
            })
    }
}