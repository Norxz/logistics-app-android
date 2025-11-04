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
 * A [Fragment] subclass that displays a history of completed delivery requests.
 *
 * This fragment is responsible for fetching and showing delivery requests that
 * have a status of 'ENTREGADA' (Delivered) or 'CANCELADA' (Canceled) within
 * the logistic zone assigned to the current user (branch).
 * It uses a [RecyclerView] to list these historical requests. If no completed
 * requests are found for the user's zone, it displays a message indicating
 * an empty history.
 *
 * This fragment corresponds to the second tab in the `BranchPagerAdapter`.
 */
class BranchCompletedFragment : Fragment() {

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

        loadCompletedRequests()
    }

    private fun loadCompletedRequests() {
        val zona = sessionManager.getZona() ?: return

        // TODO: Crear un método en SolicitudRepository para obtener solicitudes 'ENTREGADA'/'CANCELADA' por zona
        val completedItems = emptyList<SolicitudRepository.SolicitudItem>() // Simulación

        if (completedItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // adapter.updateData(completedItems)
        } else {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmpty.text = "No hay historial de envíos en tu zona."
        }
    }
}