package co.edu.unipiloto.myapplication.fragment

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
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.dto.toModel
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager
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

        // 🏆 CORRECCIÓN DE ERROR (Línea 57):
        // Se debe pasar un lambda con 3 argumentos (Solicitud, String, Long?)
        // aunque el tercero (gestorId) se ignore aquí.
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action, gestorId -> // ✅ Aceptar el tercer argumento
                handleAssignedAction(solicitud, action) // ✅ Llamar a la función con solo 2 argumentos
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
     * Función que maneja las acciones dentro de AssignedRequestsFragment (si las hay, como Cancelar).
     * Nota: Esta función solo necesita dos parámetros.
     */
    private fun handleAssignedAction(solicitud: Solicitud, action: String) {
        Log.d("AssignedFrag", "Acción: $action en solicitud ${solicitud.id}")
        // Aquí iría la lógica para manejar las acciones permitidas en esta pestaña
        // (ej. Navegar a detalles o Cancelar la solicitud si fuera permitido).
    }


    /**
     * Carga las solicitudes asignadas usando el ID de la Sucursal del Gestor/Gerente.
     */
    private fun loadAssignedRequests() {

        // ... (resto del código loadAssignedRequests sin cambios) ...

        val sucursalId = sessionManager.getBranchId() ?: run {
            tvNoRequests.visibility = View.VISIBLE
            tvNoRequests.text = getString(R.string.error_no_branch_id)
            recyclerView.visibility = View.GONE
            return
        }

        RetrofitClient.getSolicitudApi().getAssignedSolicitudesBySucursal(sucursalId).enqueue(object :
            Callback<List<SolicitudResponse>> {

            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {
                val assignedResponses = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    val assignedItems = assignedResponses.map { it.toModel() }

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