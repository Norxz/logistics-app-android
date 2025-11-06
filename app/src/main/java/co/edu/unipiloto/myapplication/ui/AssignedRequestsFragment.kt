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

    /**
     * Inflates the fragment layout that contains the generic branch/management list UI.
     *
     * @return The root View of the inflated R.layout.fragment_branch_list.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usa el layout que contiene la lista genérica de sucursal/gestión.
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    /**
     * Initializes repositories and UI components, configures the RecyclerView adapter, and loads assigned requests.
     *
     * Initializes SolicitudRepository and SessionManager, binds the RecyclerView and empty-state TextView,
     * sets a vertical LinearLayoutManager, attaches a BranchSolicitudAdapter with an empty list, and invokes
     * loadAssignedRequests() to populate the list for the current session zone.
     */
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
     * Loads requests in the "ASIGNADA" or "EN CAMINO" states for the current manager zone and updates the UI.
     *
     * Retrieves the zone from the session; if absent, the method returns early. Fetches the assigned requests
     * for that zone and updates the RecyclerView adapter when data is present, or shows the empty-state TextView
     * with the `no_assigned_requests` message when no items are found.
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