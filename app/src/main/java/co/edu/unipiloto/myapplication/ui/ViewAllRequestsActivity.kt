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
// ✅ CORRECCIÓN 1: Adaptador y Modelo deben manejar Solicitud
import co.edu.unipiloto.myapplication.adapters.RequestAdapter // Manteniendo el nombre de tu archivo
import co.edu.unipiloto.myapplication.model.Solicitud // 🏆 Usamos el modelo correcto: Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import co.edu.unipiloto.myapplication.dto.RetrofitClient.getSolicitudApi // 👈 Importamos el método API
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

    // ✅ CORRECCIÓN 1: No se esperan argumentos de tipo genérico en RequestAdapter (Línea 35)
    private lateinit var adapter: RequestAdapter
    private var requestList: MutableList<Solicitud> = mutableListOf() // La lista contiene Solicitud

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_requests)

        supportActionBar?.hide()

        // Inicialización de la lógica
        sessionManager = SessionManager(this)

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

        // La inicialización es correcta ahora que la declaración fue simplificada.
        adapter = RequestAdapter(requestList) { solicitud ->
            handleManageRequestClick(solicitud)
        }
        recyclerViewRequests.adapter = adapter
    }

    /**
     * Carga todas las solicitudes del sistema usando el servicio REST.
     */
    private fun loadRequests() {
        getSolicitudApi().getAllSolicitudes().enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val fetchedRequests = response.body()

                if (response.isSuccessful && fetchedRequests != null) {
                    if (fetchedRequests.isNotEmpty()) {
                        // ✅ CORRECCIÓN 2: Usar el método updateData del adaptador
                        adapter.updateData(fetchedRequests)
                    } else {
                        Toast.makeText(this@ViewAllRequestsActivity, "No hay solicitudes pendientes.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AdminRequests", "Error ${response.code()} al cargar solicitudes.")
                    Toast.makeText(this@ViewAllRequestsActivity, "Error al cargar datos del servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("AdminRequests", "Fallo de red: ${t.message}")
                Toast.makeText(this@ViewAllRequestsActivity, "Fallo de red. Verifique el servidor.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleManageRequestClick(solicitud: Solicitud) {
        Toast.makeText(this, "Gestionando Guía: ${solicitud.guia.id}", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, RequestDetailActivity::class.java)
        intent.putExtra("REQUEST_DATA", solicitud)
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