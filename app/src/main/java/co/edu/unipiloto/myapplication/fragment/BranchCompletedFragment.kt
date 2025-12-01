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
 * Fragmento que muestra un historial de solicitudes completadas (ENTREGADA/CANCELADA)
 * dentro de la sucursal logística del Gestor/Funcionario.
 */
class BranchCompletedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var sessionManager: SessionManager

    private lateinit var adapter: SolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🏆 CORRECCIÓN DE ERROR (Línea 55):
        // Cambiamos el lambda a tres parámetros para coincidir con el SolicitudAdapter.
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action, gestorId -> // ✅ Se incluye gestorId: Long?
                Log.d(
                    "CompletedFrag",
                    "Acción: $action en historial ${solicitud.id}. No se procesa."
                )
                // Aquí podrías llamar a una función handleCompletedAction(solicitud, action)
                // si se requiriera lógica de acción.
            }
        )
        recyclerView.adapter = adapter

        loadCompletedRequests()
    }

    override fun onResume() {
        super.onResume()
        loadCompletedRequests()
    }


    /**
     * Carga las solicitudes de historial (ENTREGADA/CANCELADA/FINALIZADA) usando el servicio REST.
     */
    private fun loadCompletedRequests() {
        // ... (resto del código sin cambios) ...
        val sucursalId = sessionManager.getBranchId() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_branch_id)
            recyclerView.visibility = View.GONE
            return
        }

        RetrofitClient.getSolicitudApi().getCompletedSolicitudesBySucursal(sucursalId).enqueue(object :
            Callback<List<SolicitudResponse>> {

            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {

                val assignedResponses = response.body() ?: emptyList()

                if (response.isSuccessful) {

                    val completedItems = assignedResponses.map { it.toModel() }

                    if (completedItems.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(completedItems)
                    } else {
                        recyclerView.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = getString(R.string.no_completed_requests)
                    }
                } else {
                    Log.e("CompletedFrag", "Error ${response.code()} al cargar historial.")
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Error al conectar con el servidor: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                Log.e("CompletedFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}