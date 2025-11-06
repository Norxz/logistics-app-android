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
import co.edu.unipiloto.myapplication.db.UserRepository

/**
 * Fragmento que lista las solicitudes pendientes de asignación para el Manager/Gestor.
 * Usa fragment_entregas_pendientes.xml.
 */
class PendingRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoRequests: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    private lateinit var adapter: SolicitudAsignacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entregas_pendientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewAssigned)
        tvNoRequests = view.findViewById(R.id.tvNoPendingRequests)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 1. Obtener la lista de conductores
        val conductoresList = userRepository.getDriversForAssignment()

        // 2. Crear el adaptador con el repositorio y el callback
        adapter = SolicitudAsignacionAdapter(
            items = emptyList(),
            conductores = conductoresList,
            solicitudRepository = solicitudRepository,
            onAssignmentSuccess = {
                // Callback: Si la asignación fue exitosa, recargamos la lista
                loadPendingRequests()
            }
        )
        recyclerView.adapter = adapter

        loadPendingRequests()
    }

    private fun loadPendingRequests() {
        val zona = sessionManager.getZona() ?: return

        // SolicitudRepository.pendientesPorZona devuelve List<Solicitud>, que es lo que necesita el adaptador.
        val pendingItems = solicitudRepository.pendientesPorZona(zona)

        if (pendingItems.isNotEmpty()) {
            tvNoRequests.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(pendingItems)
        } else {
            tvNoRequests.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}