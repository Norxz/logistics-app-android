package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.model.Solicitud // 👈 Modelo de Respuesta REST
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que muestra las solicitudes que ya han sido asignadas y están "En Ruta".
 * (Pestaña 1 de BranchPagerAdapter)
 */
class BranchInRouteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    // ❌ ELIMINADA: private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager

    // 1. Declarar el adaptador usando el modelo Solicitud
    private lateinit var adapter: SolicitudAdapter

    private var userRole: String = "GESTOR"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar gestores
        // ❌ ELIMINADA: solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())
        userRole = sessionManager.getRole() ?: "GESTOR"

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 2. Inicializar el adaptador correctamente:
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action ->
                Log.d("InRouteFrag", "Acción: $action en solicitud ${solicitud.id}")
                // Aquí se llamaría a un endpoint PUT/POST para cambiar estado o reasignar
            }
        )
        recyclerView.adapter = adapter

        loadInRouteRequests()
    }

    override fun onResume() {
        super.onResume()
        // Aseguramos que se recargan los datos cada vez que el fragmento se hace visible
        loadInRouteRequests()
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA' o 'EN RUTA' para la zona del gestor,
     * usando el servicio REST.
     */
    private fun loadInRouteRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 LLAMADA A RETROFIT: Usamos el endpoint para solicitudes asignadas/en ruta por zona
        RetrofitClient.apiService.getAssignedSolicitudesByZone(zona).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val assignedItems = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    if (assignedItems.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(assignedItems) // Pasa los datos al adaptador
                    } else {
                        recyclerView.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = getString(R.string.no_assigned_requests)
                    }
                } else {
                    Log.e("InRouteFrag", "Error ${response.code()} al cargar asignadas.")
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Error al conectar con el servidor: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("InRouteFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}