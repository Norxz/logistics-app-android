// Archivo: co.edu.unipiloto.myapplication.ui/BranchInRouteFragment.kt
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
 * Fragmento que muestra las solicitudes que ya han sido asignadas y están "En Ruta".
 * (Pestaña 1 de BranchPagerAdapter)
 */
class BranchInRouteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager

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

        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // TODO: Inicializar BranchSolicitudAdapter para esta vista

        loadInRouteRequests()
    }

    private fun loadInRouteRequests() {
        val zona = sessionManager.getZona() ?: return

        // TODO: Crear un método en SolicitudRepository para obtener solicitudes 'EN RUTA' por zona
        val inRouteItems = emptyList<SolicitudRepository.SolicitudItem>() // Simulación

        if (inRouteItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // adapter.updateData(inRouteItems)
        } else {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmpty.text = "No hay envíos actualmente en ruta."
        }
    }
}