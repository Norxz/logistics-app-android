package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.LogisticUserAdapter
import co.edu.unipiloto.myapplication.model.LogisticUser
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 Cliente REST
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewLogisticUsersActivity : AppCompatActivity() {

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var adapter: LogisticUserAdapter
    private lateinit var btnAddUser: MaterialButton
    private val userList = mutableListOf<LogisticUser>() // List used by the adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_logistic_users)

        supportActionBar?.hide()


        initViews()
        setupRecyclerView()
        setupListeners()
        // loadUsers() will be called in onResume
    }

    override fun onResume() {
        super.onResume()
        // Load and refresh users every time the activity becomes visible
        loadUsers()
    }

    private fun initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        btnAddUser = findViewById(R.id.btnAddUser)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAddUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("IS_ADMIN_REGISTERING", true)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = LogisticUserAdapter(
            onEditClick = { user -> handleEditUserClick(user) },
            onDeleteClick = { user -> handleDeleteUserClick(user) }
        )

        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = adapter

        // Aquí se pasa la lista real de usuarios
        adapter.submitList(userList)
    }


    /**
     * Loads all logistical users from the REST backend.
     */
    private fun loadUsers() {
        // 🏆 LLAMADA A RETROFIT (GET ALL)
        RetrofitClient.apiService.getAllLogisticUsers().enqueue(object : Callback<List<LogisticUser>> {
            override fun onResponse(call: Call<List<LogisticUser>>, response: Response<List<LogisticUser>>) {
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!
                    userList.clear()
                    userList.addAll(users)
                    adapter.notifyDataSetChanged() // Refresh UI
                } else {
                    Log.e("LogUserMgmt", "Error ${response.code()} al cargar usuarios.")
                    Toast.makeText(this@ViewLogisticUsersActivity, "Error al cargar personal logístico.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<LogisticUser>>, t: Throwable) {
                Log.e("LogUserMgmt", "Fallo de red: ${t.message}")
                Toast.makeText(this@ViewLogisticUsersActivity, "Fallo de red. Servidor no disponible.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleEditUserClick(user: LogisticUser) {
        val intent = Intent(this, EditLogisticUserActivity::class.java)
        intent.putExtra("RECOLECTOR_ID", user.id)
        startActivity(intent)
    }

    /**
     * Shows confirmation dialog and calls the REST endpoint to delete the user.
     */
    private fun handleDeleteUserClick(user: LogisticUser) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.fullName}? Esta acción es permanente y eliminará también el usuario asociado.")
            .setPositiveButton("Eliminar") { dialog, which ->
                deleteUserFromRest(user.userId, user.fullName) // Use the user FK (userId) if that's what the endpoint expects
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUserFromRest(userId: Long?, userName: String) {
        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🏆 LLAMADA A RETROFIT (DELETE)
        RetrofitClient.apiService.deleteLogisticUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ViewLogisticUsersActivity, "$userName eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    loadUsers() // Recargar la lista después de la eliminación
                } else {
                    Log.e("LogUserMgmt", "Fallo al eliminar: ${response.code()}")
                    Toast.makeText(this@ViewLogisticUsersActivity, "Error al eliminar a $userName. Código: ${response.code()}.", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ViewLogisticUsersActivity, "Fallo de red al eliminar.", Toast.LENGTH_LONG).show()
            }
        })
    }
}