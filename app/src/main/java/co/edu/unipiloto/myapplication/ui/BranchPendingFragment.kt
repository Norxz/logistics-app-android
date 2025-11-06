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
    // private lateinit var adapter: BranchSolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

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