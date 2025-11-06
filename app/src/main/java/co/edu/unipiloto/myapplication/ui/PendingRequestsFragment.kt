package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter // 🏆 Usamos el adaptador genérico
import co.edu.unipiloto.myapplication.db.SolicitudRepository
import co.edu.unipiloto.myapplication.db.UserRepository
import co.edu.unipiloto.myapplication.models.Solicitud // Usamos el modelo Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * Fragmento que muestra las solicitudes en la sucursal pendientes de ser asignadas.
 * (Pestaña 0 de BranchPagerAdapter)
 */
class BranchPendingFragment : Fragment() { // Usamos el nombre que ya corregimos

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView // Cambiado de tvNoRequests a tvEmpty (usado en el layout de branch)
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    private lateinit var adapter: SolicitudAdapter // 🏆 El adaptador genérico

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asumo que estás usando el layout correcto para la lista de sucursal
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        solicitudRepository = SolicitudRepository(requireContext())
        userRepository = UserRepository(requireContext())
        sessionManager = SessionManager(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewBranchList) // Usando ID del layout branch
        tvEmpty = view.findViewById(R.id.tvBranchEmpty) // Usando ID del layout branch

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador correctamente:
        adapter = SolicitudAdapter(
            items = emptyList<Solicitud>(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                // La acción del GESTOR en PENDIENTES es siempre ASIGNAR
                if (action == "ASIGNAR") {
                    handleAssignmentAction(solicitud)
                }
            }
        )
        recyclerView.adapter = adapter

        loadPendingRequests()
    }

    override fun onResume() {
        super.onResume()
        loadPendingRequests()
    }

    /**
     * Carga las solicitudes que están en estado 'PENDIENTE' para la zona del gestor.
     */
    private fun loadPendingRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // 🏆 CORRECCIÓN DE FUNCIÓN: Usamos el método enriquecido correcto
        val pendingItems = solicitudRepository.getSolicitudesPendientesEnriquecidasPorZona(zona)

        if (pendingItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(pendingItems)
        } else {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmpty.text = getString(R.string.no_pending_requests)
        }
    }

    /**
     * Maneja la lógica de asignar un recolector a una solicitud (usando el primer conductor disponible).
     */
    private fun handleAssignmentAction(solicitud: Solicitud) {
        val availableRecolector = userRepository.getFirstRecolectorByZone(solicitud.zona)

        if (availableRecolector != null) {
            val rowsAffected = solicitudRepository.asignarRecolector(
                solicitudId = solicitud.id,
                recolectorId = availableRecolector.id
            )

            if (rowsAffected > 0) {
                Toast.makeText(requireContext(), "Solicitud #${solicitud.id} asignada a ${availableRecolector.name}.", Toast.LENGTH_LONG).show()
                // Recargar la lista para que la solicitud desaparezca de Pendientes
                loadPendingRequests()
            } else {
                Toast.makeText(requireContext(), "Error al asignar la solicitud.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No hay recolectores disponibles en ${solicitud.zona}.", Toast.LENGTH_LONG).show()
        }
    }
}