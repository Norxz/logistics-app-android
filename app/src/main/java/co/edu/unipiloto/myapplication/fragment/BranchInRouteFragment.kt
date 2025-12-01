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

        // 🏆 CORRECCIÓN DE ERROR (Línea 58):
        // Se debe pasar un lambda con 3 argumentos (Solicitud, String, Long?)
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action, gestorId -> // ✅ Aceptamos el tercer parámetro
                handleInRouteAction(solicitud, action) // Llamamos a la función de manejo de acciones
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
     * Función que maneja las acciones dentro de BranchInRouteFragment.
     * Solo necesita dos parámetros.
     */
    private fun handleInRouteAction(solicitud: Solicitud, action: String) {
        // ✅ Esta función es la que antes estaba implícita en el lambda
        Log.d("InRouteFrag", "Acción: $action en solicitud ${solicitud.id}")
        // Aquí iría la lógica para manejar las acciones permitidas en esta pestaña (ej. Cancelar).
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA' o 'EN RUTA' para la sucursal del gestor,
     * usando el servicio REST.
     */
    private fun loadInRouteRequests() {
        // ... (el resto del código loadInRouteRequests sin cambios) ...

        val sucursalId = sessionManager.getBranchId() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_branch_id)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 CORRECCIÓN 2: Usar getSolicitudApi() y esperar List<SolicitudResponse>
        RetrofitClient.getSolicitudApi().getAssignedSolicitudesBySucursal(sucursalId).enqueue(object :
            Callback<List<SolicitudResponse>> {

            override fun onResponse(call: Call<List<SolicitudResponse>>, response: Response<List<SolicitudResponse>>) {

                val assignedResponses = response.body() ?: emptyList()

                if (response.isSuccessful) {

                    // Mapear DTO (Response) a Modelo (Solicitud)
                    val assignedItems = assignedResponses.map { it.toModel() }

                    if (assignedItems.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(assignedItems)
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

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                Log.e("InRouteFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}