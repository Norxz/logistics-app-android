package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.LogisticUserAdapter // Debes crear este Adapter
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.LogisticUser // Debes crear este modelo de datos
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class LogisticUserManagementActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAddUser: FloatingActionButton
    private lateinit var userAdapter: LogisticUserAdapter

    // Lista de trabajo para la búsqueda
    private var allUsers: List<LogisticUser> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistic_user_management)

        supportActionBar?.title = "Gestión Logística"

        userRepository = UserRepository(this)

        initViews()
        setupRecyclerView()
        loadUsers()
        setupListeners()
    }

    private fun initViews() {
        rvUsers = findViewById(R.id.rvLogisticUsers)
        etSearch = findViewById(R.id.etSearch)
        fabAddUser = findViewById(R.id.fabAddUser)
    }

    private fun setupRecyclerView() {
        // El adaptador manejará las acciones de Modificar y Eliminar
        userAdapter = LogisticUserAdapter(
            onEditClick = { user -> navigateToEditUser(user) },
            onDeleteClick = { user -> confirmAndDeleteUser(user) }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter
    }

    // Simula la carga de datos de la base de datos
    private fun loadUsers() {
        allUsers = userRepository.getAllLogisticUsers()

        userAdapter.submitList(allUsers)
    }

    private fun setupListeners() {
        // Acción CREAR: Navegar a RegisterActivity en modo Admin
        fabAddUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra(RegisterActivity.EXTRA_IS_ADMIN_REGISTER, true)
            startActivity(intent)
        }

        // Acción BUSCAR/FILTRAR
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        val lowerCaseQuery = query.lowercase()
        val filteredList = allUsers.filter { user ->
            user.email.lowercase().contains(lowerCaseQuery) ||
                    user.name.lowercase().contains(lowerCaseQuery) ||
                    user.role.lowercase().contains(lowerCaseQuery)
        }
        userAdapter.submitList(filteredList)
    }

    // Acción MODIFICAR: Navegar a la pantalla de detalle/edición
    private fun navigateToEditUser(user: LogisticUser) {
        // Aquí se navegaría a una Activity de detalle/edición pasando el ID del usuario
        // val intent = Intent(this, UserDetailActivity::class.java)
        // intent.putExtra("USER_ID", user.id)
        // startActivity(intent)
        Toast.makeText(this, "Modificar usuario: ${user.name}", Toast.LENGTH_SHORT).show()
    }

    // Acción ELIMINAR: Mostrar diálogo de confirmación y eliminar
    private fun confirmAndDeleteUser(user: LogisticUser) {
        // TODO: Implementar un AlertDialog para confirmar la eliminación
        // Después de la confirmación:
        // val success = userRepository.deleteUser(user.id)
        // if (success) { loadUsers() }

        Toast.makeText(
            this,
            "Eliminar usuario (Confirmar diálogo): ${user.name}",
            Toast.LENGTH_LONG
        ).show()
        // Recargar lista para reflejar el cambio (simulado)
        loadUsers()
    }

    // Recargar datos cuando volvemos a esta Activity (ej: después de crear o editar)
    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}