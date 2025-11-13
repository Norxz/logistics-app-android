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
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.models.LogisticUser
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import co.edu.unipiloto.myapplication.storage.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
/**
 * Fragmento que muestra las solicitudes en la sucursal pendientes de ser asignadas.
 * (Pestaña 0 de BranchPagerAdapter)
 */
class BranchPendingFragment : Fragment() { // Usamos el nombre que ya corregimos

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView // Cambiado de tvNoRequests a tvEmpty (usado en el layout de branch)
    private lateinit var sessionManager: SessionManager

    private lateinit var adapter: SolicitudAdapter // 🏆 El adaptador genérico

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asumo que estás usando el layout correcto para la lista de sucursal
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewBranchList) // Usando ID del layout branch
        tvEmpty = view.findViewById(R.id.tvBranchEmpty) // Usando ID del layout branch

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador correctamente:
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                // La acción del GESTOR en PENDIENTES es siempre ASIGNAR
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

    /**
     * Carga las solicitudes que están en estado 'PENDIENTE' para la zona del gestor.
     */
    private fun loadPendingRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 LLAMADA A RETROFIT: Endpoint para solicitudes PENDIENTES por zona
        RetrofitClient.apiService.getPendingSolicitudesByZone(zona).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val pendingItems = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    if (pendingItems.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(pendingItems)
                    } else {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        tvEmpty.text = getString(R.string.no_pending_requests)
                    }
                } else {
                    Log.e("PendingFrag", "Error ${response.code()} al cargar pendientes.")
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Error al conectar con el servidor: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("PendingFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }

    /**
     * Maneja la lógica de asignar un recolector a una solicitud (usando el primer conductor disponible).
     */
    private fun handleAssignmentAction(solicitud: Solicitud) {
        // La lógica de seleccionar el mejor conductor debe estar en el backend.
        // Aquí solo enviamos la solicitud al backend para que él decida y asigne.

        // Primero, intentamos obtener un conductor disponible (Necesitas este endpoint en el backend)
        // Usaremos el ID de la solicitud y la zona para que el backend encuentre al conductor adecuado.

        RetrofitClient.apiService.getAvailableDriverByZone(solicitud.zona).enqueue(object : Callback<LogisticUser> {
            override fun onResponse(call: Call<LogisticUser>, response: Response<LogisticUser>) {
                if (response.isSuccessful && response.body() != null) {
                    val availableRecolector = response.body()!!
                    assignRequest(solicitud.id, availableRecolector)
                } else {
                    Toast.makeText(requireContext(), "No hay recolectores disponibles en ${solicitud.zona}.", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<LogisticUser>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red al buscar conductor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Llama al endpoint REST para asignar la solicitud al conductor encontrado.
     */
    private fun assignRequest(solicitudId: Long, recolector: LogisticUser) {
        val requestBody = mapOf("recolectorId" to recolector.id.toString())

        // 🏆 LLAMADA REST PARA ASIGNAR (PUT /solicitudes/{id}/assign)
        RetrofitClient.apiService.assignRequest(solicitudId, requestBody).enqueue(object : Callback<Solicitud> {
            override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Solicitud #${solicitudId} asignada a ${recolector.name}!", Toast.LENGTH_LONG).show()
                    loadPendingRequests() // Recargar la lista para actualizar la UI
                } else {
                    Toast.makeText(requireContext(), "Error al asignar la solicitud: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                Toast.makeText(requireContext(), "Fallo de red al asignar.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}