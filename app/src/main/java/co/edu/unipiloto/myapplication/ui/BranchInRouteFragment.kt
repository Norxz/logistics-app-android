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
// Importamos el adaptador genérico
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.db.SolicitudRepository
// Importamos el modelo de datos
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * Fragmento que muestra las solicitudes que ya han sido asignadas y están "En Ruta".
 * (Pestaña 1 de BranchPagerAdapter, que incluye estados: ASIGNADA, EN_RECOLECCION, RECOGIDA)
 */
class BranchInRouteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager

    // 1. Declarar el adaptador usando el modelo Solicitud
    private lateinit var adapter: SolicitudAdapter

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

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 2. Inicializar el adaptador correctamente:
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(), // Especificamos el tipo de lista
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                // Lógica de acción del Gestor/Funcionario:
                // Puede ser reasignar, cancelar, o ver detalles.
                Log.d("InRouteFrag", "Acción: $action en solicitud ${solicitud.id}")
            }
        )
        recyclerView.adapter = adapter

        loadInRouteRequests()
    }

    override fun onResume() {
        super.onResume()
        // Aseguramos que se recargan los datos cada vez que el fragmento se hace visible
        loadInRouteRequests()
    }

    /**
     * Carga las solicitudes que ya están en estado 'ASIGNADA' o 'EN RUTA' para la zona del gestor.
     */
    private fun loadInRouteRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // 3. Usar el método existente y correcto del repositorio:
        // getSolicitudesAsignadasEnriquecidasPorZona engloba los estados 'en ruta'.
        val inRouteItems = solicitudRepository.getSolicitudesAsignadasEnriquecidasPorZona(zona)

        if (inRouteItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(inRouteItems) // Pasa los datos al adaptador
        } else {
            // Muestra mensaje si no hay solicitudes en ruta
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            // Usamos un string más descriptivo
            tvEmpty.text = getString(R.string.no_assigned_requests)
        }
    }
}