package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.LogisticUser
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que muestra las solicitudes en la sucursal pendientes de ser asignadas.
 * (Pestaña 0 de BranchPagerAdapter)
 */
class BranchPendingFragment : Fragment() {

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

        adapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                if (action == "ASIGNAR") {
                    handleAssignmentAction(solicitud)
                }
            }
        )

        recyclerView.adapter = adapter

        loadPendingRequests()
    }

    override fun onResume() {
        super.onResume()
        loadPendingRequests()
    }

    private fun loadPendingRequests() {

        val sucursalId = sessionManager.getSucursalId() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Error: No se encontró la sucursal del usuario."
            recyclerView.visibility = View.GONE
            return
        }

        RetrofitClient.apiService.getSolicitudesBySucursal(sucursalId)
            .enqueue(object : Callback<List<Solicitud>> {

                override fun onResponse(
                    call: Call<List<Solicitud>>,
                    response: Response<List<Solicitud>>
                ) {
                    if (response.isSuccessful) {

                        val solicitudes = response.body()?.filter { it.estado == "PENDIENTE" }
                            ?: emptyList()

                        if (solicitudes.isNotEmpty()) {
                            tvEmpty.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.updateData(solicitudes)
                        } else {
                            tvEmpty.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            tvEmpty.text = "No hay solicitudes pendientes en esta sucursal."
                        }

                    } else {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Error del servidor: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Fallo de red."
                }
            })
    }

    private fun handleAssignmentAction(solicitud: Solicitud) {

        val sucursalId = sessionManager.getSucursalId() ?: return

        RetrofitClient.apiService.getAvailableDriverBySucursal(sucursalId)
            .enqueue(object : Callback<LogisticUser> {

                override fun onResponse(
                    call: Call<LogisticUser>,
                    response: Response<LogisticUser>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        assignRequest(solicitud.id, response.body()!!)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No hay recolectores disponibles.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LogisticUser>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Error de red al buscar conductor.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun assignRequest(solicitudId: Long, recolector: LogisticUser) {

        val body = mapOf("recolectorId" to recolector.id.toString())

        RetrofitClient.apiService.assignRequest(solicitudId, body)
            .enqueue(object : Callback<Solicitud> {

                override fun onResponse(
                    call: Call<Solicitud>,
                    response: Response<Solicitud>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Solicitud asignada correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadPendingRequests()
                    }
                }

                override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Fallo de red al asignar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
