package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.LogisticUser
import co.edu.unipiloto.myapplication.ui.adapters.LogisticUserAdapter
import com.google.android.material.button.MaterialButton

class ViewLogisticUsersActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var adapter: LogisticUserAdapter
    private lateinit var btnAddUser: MaterialButton
    private val userList = mutableListOf<LogisticUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate que estás usando el layout correcto
        setContentView(R.layout.activity_view_logistic_users)

        supportActionBar?.hide()

        userRepository = UserRepository(this)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadUsers()
    }

    private fun initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        btnAddUser = findViewById(R.id.btnAddUser)
    }

    private fun setupListeners() {
        // Botón Atrás
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Botón Agregar Nuevo Usuario (Abre RegisterActivity o una nueva Activity específica)
        btnAddUser.setOnClickListener {
            // TODO: Crear una Activity específica para registrar personal logístico
            // Por ahora, redirigiremos a RegisterActivity, asumiendo que el admin
            // seleccionará el rol correcto allí.
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("IS_ADMIN_REGISTERING", true) // Pasa un flag para diferenciar
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        // Inicializa el adaptador con el nuevo callback que maneja los 2 tipos de acción
        adapter = LogisticUserAdapter(userList) { user, actionType ->
            when (actionType) {
                LogisticUserAdapter.ActionType.EDIT -> handleEditUserClick(user)
                LogisticUserAdapter.ActionType.DELETE -> handleDeleteUserClick(user)
            }
        }
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = adapter
    }

    private fun loadUsers() {
        val users = userRepository.getAllLogisticUsers()
        userList.clear()
        userList.addAll(users)
        adapter.notifyDataSetChanged()
    }

    private fun handleEditUserClick(user: LogisticUser) {
        // TODO: Implementar EditLogisticUserActivity
        Toast.makeText(this, "Editar usuario: ${user.name} (ID: ${user.id})", Toast.LENGTH_SHORT).show()
        // val intent = Intent(this, EditLogisticUserActivity::class.java)
        // intent.putExtra("RECOLECTOR_ID", user.id)
        // startActivity(intent)
    }

    private fun handleDeleteUserClick(user: LogisticUser) {
        // Diálogo de confirmación antes de eliminar permanentemente
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}? Esta acción es permanente y eliminará también el usuario asociado.")
            .setPositiveButton("Eliminar") { dialog, which ->
                val success = userRepository.deleteLogisticUser(user.id)
                if (success) {
                    Toast.makeText(this, "${user.name} eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    loadUsers() // Recargar la lista después de la eliminación
                } else {
                    Toast.makeText(this, "Error al eliminar a ${user.name}.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Asegura que la lista se recargue si el usuario regresa de una Activity de registro/edición
    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}