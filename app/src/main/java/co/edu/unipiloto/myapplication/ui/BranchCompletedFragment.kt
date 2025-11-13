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
// Importamos el adaptador genérico que hemos desarrollado
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
// Importamos el modelo de datos para la inferencia de tipo
import co.edu.unipiloto.myapplication.models.Solicitud // DTO de Respuesta
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.rest.RetrofitClient // 👈 NUEVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que muestra un historial de solicitudes completadas (ENTREGADA/CANCELADA)
 * dentro de la zona logística del Gestor/Funcionario.
 */
class BranchCompletedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    // ❌ ELIMINADA: private lateinit var solicitudRepository: SolicitudRepository
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

        // Inicializar gestores
        // ❌ ELIMINADA: solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                Log.d("CompletedFrag", "Acción: $action en historial ${solicitud.id}. No se procesa.")
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
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 LLAMADA A RETROFIT: Usamos el endpoint para solicitudes finalizadas por zona.
        RetrofitClient.apiService.getCompletedSolicitudesByZone(zona).enqueue(object : Callback<List<Solicitud>> {
            override fun onResponse(call: Call<List<Solicitud>>, response: Response<List<Solicitud>>) {
                val completedItems = response.body() ?: emptyList()

                if (response.isSuccessful) {
                    if (completedItems.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(completedItems) // Actualiza el adaptador
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

            override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                Log.e("CompletedFrag", "Fallo de red: ${t.message}")
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Fallo de red. Verifique el servidor."
            }
        })
    }
}