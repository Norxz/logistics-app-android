// co.edu.unipiloto.myapplication.ui.AssignedRequestsFragment.kt
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
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.dto.toModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que lista las solicitudes que ya han sido asignadas a conductores para el Manager/Gestor.
 * (Pestaña Asignadas)
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

        sessionManager = SessionManager(requireContext())
        userRole = sessionManager.getRole() ?: "GESTOR"

        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action ->
                Log.d("AssignedFrag", "Acción: $action en solicitud ${solicitud.id}")
            }
        )
        recyclerView.adapter = adapter

        loadAssignedRequests()
    }

    override fun onResume() {
        super.onResume()
        loadAssignedRequests()
    }

    /**
     * Carga las solicitudes asignadas usando el ID de la Sucursal del Gestor/Gerente.
     */
    private fun loadAssignedRequests() {

        val sucursalId = sessionManager.getSucursalId() ?: run {
            tvNoRequests.visibility = View.VISIBLE
            tvNoRequests.text = getString(R.string.error_no_branch_id)
            recyclerView.visibility = View.GONE
            return
        }

        // 🚨 ASUMO: RetrofitClient.getSolicitudApi() es el método correcto para obtener el servicio
        RetrofitClient.getSolicitudApi().getAssignedSolicitudesBySucursal(sucursalId).enqueue(object : Callback<List<SolicitudResponse>> {

            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {

                val assignedResponses = response.body() ?: emptyList()

                if (response.isSuccessful) {

                    // 3. Mapeo de DTO a Modelo local
                    val assignedItems = assignedResponses.map { it.toModel() } // Usa la función de extensión

                    if (assignedItems.isNotEmpty()) {
                        tvNoRequests.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(assignedItems)
                    } else {
                        tvNoRequests.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        tvNoRequests.text = getString(R.string.no_assigned_requests)
                    }
                } else {
                    Log.e("AssignedFrag", "Error ${response.code()} al cargar asignadas.")
                    tvNoRequests.visibility = View.VISIBLE
                    tvNoRequests.text = getString(R.string.error_server_code, response.code())
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                Log.e("AssignedFrag", "Fallo de red: ${t.message}")
                tvNoRequests.visibility = View.VISIBLE
                tvNoRequests.text = getString(R.string.error_network_fail)
            }
        })
    }
}