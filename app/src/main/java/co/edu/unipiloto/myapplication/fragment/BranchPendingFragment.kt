package co.edu.unipiloto.myapplication.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.databinding.FragmentBranchListBinding
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.UserRepository
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardVMFactory
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento que muestra las solicitudes en la sucursal pendientes de ser asignadas.
 * (Pestaña 0 de BranchPagerAdapter)
 */
class BranchPendingFragment : Fragment() {

    private var _binding: FragmentBranchListBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SolicitudAdapter
    private lateinit var viewModel: ManagerDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupViewModel()
        setupObservers()

        binding.recyclerViewBranchList.layoutManager = LinearLayoutManager(requireContext())

        // 🏆 CORRECCIÓN 1: Implementar el listener con 3 argumentos y la lógica de asignación
        adapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action, gestorId -> // ✅ Aceptamos los 3 argumentos
                if (action == "ASIGNAR" && gestorId != null) {
                    assignRequestToManager(solicitud, gestorId)
                } else if (action == "CANCELAR_CLIENTE") {
                    // Si el gestor pudiera cancelar, la lógica iría aquí, usando solo solicitud y action
                    Log.d(
                        "PendingFrag",
                        "Acción: $action en solicitud ${solicitud.id}. No implementada."
                    )
                }
            }
        )

        binding.recyclerViewBranchList.adapter = adapter

        // 🌟 Cargar gestores disponibles para el Spinner del adapter
        viewModel.loadAvailableGestores()

        loadPendingRequests()
    }

    // --- MÉTODOS DE VIEWMODEL ---

    private fun setupViewModel() {
        // Inicialización manual (sin Dagger/Hilt)
        val solicitudRepo = SolicitudRepository(RetrofitClient.getSolicitudApi())
        val userRepo = UserRepository(RetrofitClient.getUserApi())

        val factory = ManagerDashboardVMFactory(solicitudRepo, userRepo)
        viewModel = ViewModelProvider(this, factory)[ManagerDashboardViewModel::class.java]
    }

    private fun setupObservers() {
        // Observar solicitudes pendientes
        viewModel.pendingSolicitudes.observe(viewLifecycleOwner) { solicitudes ->
            if (solicitudes.isNotEmpty()) {
                binding.tvBranchEmpty.visibility = View.GONE
                binding.recyclerViewBranchList.visibility = View.VISIBLE
                adapter.updateData(solicitudes)
            } else {
                binding.tvBranchEmpty.visibility = View.VISIBLE
                binding.recyclerViewBranchList.visibility = View.GONE
                binding.tvBranchEmpty.text = getString(R.string.no_pending_requests_branch)
            }
        }

        // 🌟 Observar Gestores disponibles y actualizar el adaptador
        viewModel.availableGestores.observe(viewLifecycleOwner) { gestores ->
            adapter.updateUsersForAssignment(gestores)
        }

        // Observar errores (opcional)
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    // ------------------------------------

    override fun onResume() {
        super.onResume()
        // Asegurarse de que los datos se recarguen al volver al fragmento
        loadPendingRequests()
        viewModel.loadAvailableGestores() // Recargar gestores por si cambiaron
    }

    private fun loadPendingRequests() {

        // 🏆 CORRECCIÓN 2: Usar getBranchId()
        val sucursalId = sessionManager.getBranchId() ?: run {
            binding.tvBranchEmpty.visibility = View.VISIBLE
            binding.tvBranchEmpty.text = getString(R.string.error_no_branch_id)
            binding.recyclerViewBranchList.visibility = View.GONE
            return
        }

        // Usar el ViewModel para iniciar la carga con Corrutinas
        viewModel.loadBranchSolicitudes(sucursalId)
    }

    /**
     * Inicia la asignación de la solicitud al ID de gestor/recolector seleccionado.
     * Esta función reemplaza la lógica de búsqueda automática del driver.
     */
    private fun assignRequestToManager(solicitud: Solicitud, recolectorId: Long) {
        val solicitudId = solicitud.id ?: run {
            Toast.makeText(requireContext(), "Error: ID de solicitud nulo", Toast.LENGTH_SHORT).show()
            return
        }

        // Preparar el body con el ID del recolector
        val body = mapOf("recolectorId" to recolectorId.toString())

        RetrofitClient.getSolicitudApi().assignRequest(solicitudId, body)
            .enqueue(object : Callback<SolicitudResponse> {

                override fun onResponse(
                    call: Call<SolicitudResponse>,
                    response: Response<SolicitudResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.request_assigned_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        loadPendingRequests() // Recargar la lista
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_assignment_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_network_assignment),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}