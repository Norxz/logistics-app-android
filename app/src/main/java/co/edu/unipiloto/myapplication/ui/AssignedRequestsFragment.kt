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
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 NUEVO: Cliente REST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que lista las solicitudes que ya han sido asignadas a conductores para el Manager/Gestor.
 */
class AssignedRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoRequests: TextView
    private lateinit var sessionManager: SessionManager

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
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action ->
                Log.d("AssignedFrag", "Acción: $action en solicitud ${solicitud.id}")
                // Implementar lógica de acción (reasignar, etc.)
            }
        )
        recyclerView.adapter = adapter

        loadAssignedRequests()
    }

    /**
     * Carga las solicitudes asignadas usando el servicio REST.
     */
    private fun loadAssignedRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvNoRequests.visibility = View.VISIBLE
            tvNoRequests.text = getString(R.string.error_no_zone)
            return
        }

        // 🏆 LLAMADA A RETROFIT: Asumimos el endpoint /zone/{zona}/assigned
        RetrofitClient.apiService.getAssignedSolicitudesByZone(zona).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val assignedItems = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    if (assignedItems.isNotEmpty()) {
                        tvNoRequests.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(assignedItems)
                    } else {
                        recyclerView.visibility = View.GONE
                        tvNoRequests.visibility = View.VISIBLE
                        tvNoRequests.text = getString(R.string.no_assigned_requests)
                    }
                } else {
                    Log.e("AssignedFrag", "Error ${response.code()} al cargar asignadas.")
                    tvNoRequests.visibility = View.VISIBLE
                    tvNoRequests.text = "Error al conectar con el servidor: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("AssignedFrag", "Fallo de red: ${t.message}")
                tvNoRequests.visibility = View.VISIBLE
                tvNoRequests.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}