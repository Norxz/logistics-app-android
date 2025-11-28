package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.RequestAdapter
import co.edu.unipiloto.myapplication.model.Request // Importación del modelo de datos correcto
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.dto.RetrofitClient // 👈 Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity para el administrador: Muestra todas las solicitudes del sistema.
 */
class ViewAllRequestsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var btnBack: ImageButton
    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var btnLogoutRequests: MaterialButton
    private lateinit var adapter: RequestAdapter
    private var requestList: MutableList<Request> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_requests)

        supportActionBar?.hide()

        // Inicialización de la lógica
        sessionManager = SessionManager(this)
        // ❌ ELIMINADA la inicialización de userRepository

        // 1. Verificar sesión de administrador
        if (sessionManager.getRole() != "ADMIN") {
            logoutUser()
            return
        }

        initViews()
        setupListeners()
        setupRecyclerView()

        // 2. Cargar datos desde el backend REST
        loadRequests()
    }

    override fun onResume() {
        super.onResume()
        // Asegura que los datos se recarguen al regresar (ej., después de editar un request)
        loadRequests()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        recyclerViewRequests = findViewById(R.id.recyclerViewRequests)
        btnLogoutRequests = findViewById(R.id.btnLogoutRequests)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLogoutRequests.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupRecyclerView() {
        recyclerViewRequests.layoutManager = LinearLayoutManager(this)
        recyclerViewRequests.setHasFixedSize(true)

        // Inicializamos el adaptador con la lista mutable
        adapter = RequestAdapter(requestList) { request ->
            handleManageRequestClick(request)
        }
        recyclerViewRequests.adapter = adapter
    }

    /**
     * Carga todas las solicitudes del sistema usando el servicio REST.
     */
    private fun loadRequests() {
        // 🏆 LLAMADA A RETROFIT (GET: Asumimos endpoint /api/v1/solicitudes/all)
        RetrofitClient.apiService.getAllRequests().enqueue(object : Callback<List<Request>> {
            override fun onResponse(call: Call<List<Request>>, response: Response<List<Request>>) {
                val fetchedRequests = response.body()

                if (response.isSuccessful && fetchedRequests != null) {
                    if (fetchedRequests.isNotEmpty()) {
                        requestList.clear()
                        requestList.addAll(fetchedRequests)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@ViewAllRequestsActivity, "No hay solicitudes pendientes.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AdminRequests", "Error ${response.code()} al cargar solicitudes.")
                    Toast.makeText(this@ViewAllRequestsActivity, "Error al cargar datos del servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Request>>, t: Throwable) {
                Log.e("AdminRequests", "Fallo de red: ${t.message}")
                Toast.makeText(this@ViewAllRequestsActivity, "Fallo de red. Verifique el servidor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleManageRequestClick(request: Request) {
        // Aquí defines la lógica de gestión (abrir un diálogo o una nueva Activity)
        Toast.makeText(this, "Gestionando Guía: ${request.guiaId}", Toast.LENGTH_SHORT).show()

        // EJEMPLO: Abrir RequestDetailActivity, la cual migraste en un paso anterior
        val intent = Intent(this, RequestDetailActivity::class.java)
        // Nota: Asegúrate de que el modelo Request sea Serializable si usas getSerializableExtra
        intent.putExtra("REQUEST_DATA", request)
        startActivity(intent)
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}