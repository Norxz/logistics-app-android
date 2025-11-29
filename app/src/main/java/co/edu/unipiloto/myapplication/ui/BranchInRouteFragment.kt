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
 * Fragmento que muestra las solicitudes que ya han sido asignadas y están "En Ruta".
 * (Pestaña 1 de BranchPagerAdapter)
 */
class BranchInRouteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
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
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action ->
                // ✅ 'solicitud.id' resuelto por la importación de Solicitud
                Log.d("InRouteFrag", "Acción: $action en solicitud ${solicitud.id}")
            }
        )
        recyclerView.adapter = adapter

        loadInRouteRequests()
    }

    override fun onResume() {
        super.onResume()
        loadInRouteRequests()
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA' o 'EN RUTA' para la sucursal del gestor,
     * usando el servicio REST.
     */
    private fun loadInRouteRequests() {
        // 🚨 CORRECCIÓN 1: Usar getSucursalId() en lugar de getZona()
        val sucursalId = sessionManager.getSucursalId() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_branch_id)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 CORRECCIÓN 2: Usar getSolicitudApi() y esperar List<SolicitudResponse>
        RetrofitClient.getSolicitudApi().getAssignedSolicitudesBySucursal(sucursalId).enqueue(object : Callback<List<SolicitudResponse>> {

            // 🚨 CORRECCIÓN 3: onResponse debe manejar List<SolicitudResponse>
            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {

                val assignedResponses = response.body() ?: emptyList()

                if (response.isSuccessful) {

                    // 💡 Mapear DTO (Response) a Modelo (Solicitud)
                    val assignedItems = assignedResponses.map { it.toModel() }

                    if (assignedItems.isNotEmpty()) { // ✅ isNotEmpty resuelto por el tipo List
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(assignedItems) // ✅ updateData ahora recibe Solicitud
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

            // 🚨 CORRECCIÓN 4: onFailure debe manejar List<SolicitudResponse>
            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                Log.e("InRouteFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}