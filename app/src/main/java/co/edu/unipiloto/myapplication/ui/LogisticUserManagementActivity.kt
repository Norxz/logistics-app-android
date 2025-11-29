package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import co.edu.unipiloto.myapplication.model.User
// ✅ IMPORTACIÓN CORREGIDA para que coincida con el paquete del adaptador
import co.edu.unipiloto.myapplication.adapters.LogisticUserAdapter

class LogisticUserManagementActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAddUser: FloatingActionButton
    // Tipo de adaptador ahora resuelto
    private lateinit var userAdapter: LogisticUserAdapter
    private var allUsers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistic_user_management)

        supportActionBar?.title = "Gestión Logística"
        initViews()
        setupRecyclerView()
        loadUsers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun initViews() {
        rvUsers = findViewById(R.id.rvLogisticUsers)
        etSearch = findViewById(R.id.etSearch)
        fabAddUser = findViewById(R.id.fabAddUser)
    }

    private fun setupRecyclerView() {
        // La inicialización ahora es posible
        userAdapter = LogisticUserAdapter(
            onEditClick = { user: User -> navigateToEditUser(user) },
            onDeleteClick = { user: User -> confirmAndDeleteUser(user) }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter
    }

    /**
     * Carga los usuarios logísticos desde el backend REST.
     */
    private fun loadUsers() {
        RetrofitClient.getUserApi().getAllLogisticUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    allUsers = response.body()!!
                    // submitList ahora está resuelto porque LogisticUserAdapter es reconocido
                    userAdapter.submitList(allUsers)
                } else {
                    Log.e("LogUserMgmt", "Error ${response.code()} al cargar usuarios.")
                    Toast.makeText(this@LogisticUserManagementActivity, "Error al cargar personal logístico.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("LogUserMgmt", "Fallo de red: ${t.message}")
                Toast.makeText(this@LogisticUserManagementActivity, "Fallo de red. Servidor no disponible.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupListeners() {
        fabAddUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra(RegisterActivity.EXTRA_IS_ADMIN_REGISTER, true)
            startActivity(intent)
        }

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
                    user.fullName.lowercase().contains(lowerCaseQuery) ||
                    user.role.lowercase().contains(lowerCaseQuery)
        }
        // submitList ahora está resuelto
        userAdapter.submitList(filteredList)
    }

    private fun navigateToEditUser(user: User) {
        val intent = Intent(this, EditLogisticUserActivity::class.java)
        intent.putExtra("RECOLECTOR_ID", user.id!!)
        startActivity(intent)
    }

    /**
     * Muestra diálogo de confirmación y llama al servicio REST para eliminar.
     */
    private fun confirmAndDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.fullName}? Esta acción es permanente.")
            .setPositiveButton("Eliminar") { dialog, which ->
                deleteLogisticUser(user.id!!, user.fullName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteLogisticUser(userId: Long, userName: String) {
        RetrofitClient.getUserApi().deleteLogisticUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@LogisticUserManagementActivity, "$userName eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    loadUsers() // Recargar la lista
                } else {
                    Log.e("LogUserMgmt", "Fallo al eliminar: ${response.code()}")
                    Toast.makeText(this@LogisticUserManagementActivity, "Error al eliminar a $userName. Código: ${response.code()}.", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@LogisticUserManagementActivity, "Fallo de red al eliminar.", Toast.LENGTH_LONG).show()
            }
        })
    }
}