package co.edu.unipiloto.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardViewModel
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardVMFactory

/**
 * Fragmento que muestra las Solicitudes Pendientes de Asignar (a Gestores).
 * Permite al Funcionario/Analista seleccionar un Gestor y realizar la asignación.
 */
class PendingRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView

    private lateinit var adapter: SolicitudAdapter
    private lateinit var viewModel: ManagerDashboardViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        initViews(view)
        setupViewModel()
        setupObservers()
        loadInitialData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewPending)
        tvEmptyState = view.findViewById(R.id.tvEmptyStatePending)

        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole() ?: "DEFAULT_FALLBACK_ROL",
            // Callback que recibe el ID del Gestor
            onActionClick = { solicitud, action, gestorId ->
                handleAssignmentAction(solicitud, action, gestorId)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupViewModel() {
        val solicitudRepo = SolicitudRepository(RetrofitClient.getSolicitudApi())
        val userRepo = UserRepository(RetrofitClient.getUserApi())

        val factory = ManagerDashboardVMFactory(solicitudRepo, userRepo)
        viewModel = ViewModelProvider(this, factory)[ManagerDashboardViewModel::class.java]
    }

    private fun setupObservers() {
        // Observa solicitudes pendientes
        viewModel.pendingSolicitudes.observe(viewLifecycleOwner) { solicitudes ->
            adapter.updateData(solicitudes)

            if (solicitudes.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
            }
        }

        viewModel.availableGestores.observe(viewLifecycleOwner) { gestores ->
            adapter.updateUsersForAssignment(gestores)
        }

        // Observa assignmentResult
        viewModel.assignmentResult.observe(viewLifecycleOwner) { result ->
            result?.let { finalResult ->
                finalResult.onSuccess {
                    Toast.makeText(context, "¡Asignación de Gestor exitosa!", Toast.LENGTH_SHORT).show()
                    loadInitialData()
                }

                finalResult.onFailure { exception ->
                    val errorMessage = exception.message ?: "Error desconocido al asignar."
                    Toast.makeText(context, "Fallo en la asignación: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observar errores generales del ViewModel
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadInitialData() {
        val branchId = sessionManager.getBranchId()

        if (branchId != null) {
            viewModel.loadBranchSolicitudes(branchId)
            // Llama al ViewModel para cargar usuarios con rol GESTOR
            viewModel.loadAvailableGestores()
        } else {
            Toast.makeText(context, "Error: ID de sucursal no encontrado. No se pueden cargar solicitudes.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Callback que se ejecuta cuando se presiona el botón "Asignar", usando el ID del Gestor.
     */
    private fun handleAssignmentAction(solicitud: Solicitud, action: String, gestorId: Long?) {
        if (action == "ASIGNAR") {
            if (solicitud.id != null && gestorId != null) {
                // Llama al método del ViewModel para asignar a un Gestor.
                viewModel.assignGestorToRequest(solicitud.id, gestorId)
            } else {
                Toast.makeText(context, "Error: Por favor, seleccione un Gestor válido.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}