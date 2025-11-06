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
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter // Usaremos el adaptador genérico
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.models.Solicitud
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

    // Usamos el SolicitudAdapter genérico (lo renombramos para compatibilidad)
    private lateinit var adapter: SolicitudAdapter

    // Almacenamos el rol para inicializar el adaptador correctamente
    private var userRole: String = "GESTOR"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar repositorios
        solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())
        userRole = sessionManager.getRole() ?: "GESTOR" // Obtener el rol real

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🏆 CORRECCIÓN: Inicializar y asignar adaptador.
        // 1. Especificar el tipo de lista vacía.
        // 2. Pasar el rol y un listener de acción (aunque esté vacío por ahora).
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = userRole,
            onActionClick = { solicitud, action ->
                Log.d("AssignedFrag", "Acción: $action en solicitud ${solicitud.id}")
                // Implementar lógica de acción del gestor aquí (ej: reasignar, cancelar, etc.)
            }
        )
        recyclerView.adapter = adapter

        loadAssignedRequests()
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA', 'EN RECOLECCION', etc.
     * para la zona del gestor.
     */
    private fun loadAssignedRequests() {
        // Obtenemos la zona, si es nula, salimos de la función.
        val zona = sessionManager.getZona() ?: run {
            tvNoRequests.visibility = View.VISIBLE
            tvNoRequests.text = getString(R.string.error_no_zone)
            return
        }

        // Carga las solicitudes asignadas de la zona.
        val assignedItems = solicitudRepository.getSolicitudesAsignadasEnriquecidasPorZona(zona)

        if (assignedItems.isNotEmpty()) {
            tvNoRequests.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(assignedItems)
        } else {
            // Muestra mensaje si no hay solicitudes asignadas
            recyclerView.visibility = View.GONE
            tvNoRequests.visibility = View.VISIBLE
            tvNoRequests.text = getString(R.string.no_assigned_requests)
        }
    }
}