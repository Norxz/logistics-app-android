package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * Fragmento que lista las solicitudes que ya han sido asignadas a conductores para el Manager/Gestor.
 * Usa fragment_branch_list.xml.
 */
class AssignedRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoRequests: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager

    // Usamos el BranchSolicitudAdapter que creamos en el paso anterior.
    private lateinit var adapter: BranchSolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usa el layout que contiene la lista genérica de sucursal/gestión.
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar repositorios
        solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar y asignar adaptador
        adapter = BranchSolicitudAdapter(emptyList())
        recyclerView.adapter = adapter

        loadAssignedRequests()
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA' o 'EN CAMINO' para la zona del gestor.
     */
    private fun loadAssignedRequests() {
        val zona = sessionManager.getZona() ?: return

        // Implementación con el nuevo método del repositorio:
        val assignedItems = solicitudRepository.asignadasPorZona(zona)

        if (assignedItems.isNotEmpty()) {
            tvNoRequests.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(assignedItems) // Pasa los datos al adaptador
        } else {
            // Muestra mensaje si no hay solicitudes asignadas
            tvNoRequests.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvNoRequests.text = getString(R.string.no_assigned_requests) // Asegúrate de tener este string
        }
    }
}