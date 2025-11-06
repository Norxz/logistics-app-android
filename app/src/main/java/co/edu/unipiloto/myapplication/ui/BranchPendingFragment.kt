// Archivo: co.edu.unipiloto.myapplication.ui/BranchPendingFragment.kt
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
 * Fragmento que muestra las solicitudes en la sucursal pendientes de ser asignadas
 * o procesadas por un funcionario.
 * (Pestaña 0 de BranchPagerAdapter)
 */
class BranchPendingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager
    // Asumimos un adaptador para la vista de sucursal
    /**
     * Inflates and returns the fragment's layout for displaying the branch request list.
     *
     * @return The inflated root view for this fragment's UI (fragment_branch_list).
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    /**
     * Initializes repositories and UI elements for the fragment and starts loading pending branch requests.
     *
     * Binds the RecyclerView and empty-state TextView from the provided view, configures the RecyclerView's
     * layout manager, and triggers loading of pending requests for the current session zone.
     *
     * @param view The fragment's root view used to find and bind child views.
     * @param savedInstanceState Previously saved state, if any.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())

        // Usamos los IDs del layout que solo contiene el RecyclerView de lista (ver XMLs)
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // TODO: Crear e inicializar el BranchSolicitudAdapter aquí
        // adapter = BranchSolicitudAdapter(emptyList())
        // recyclerView.adapter = adapter

        loadPendingRequests()
    }

    /**
     * Load pending branch requests for the current session zone and update the list and empty-state views.
     *
     * If the session zone is not available, the function returns without changing the UI. Otherwise it fetches
     * pending requests for that zone and shows the RecyclerView when there are items or shows the empty-state
     * TextView when there are none.
     */
    private fun loadPendingRequests() {
        val zona = sessionManager.getZona() ?: return

        // El repositorio devuelve la Solicitud completa (con ID de dirección)
        val pendingItems = solicitudRepository.pendientesPorZona(zona)

        if (pendingItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // adapter.updateData(pendingItems)
        } else {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}