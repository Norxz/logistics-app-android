package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // ⬅️ Necesario
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.databinding.FragmentBranchListBinding
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.User
import co.edu.unipiloto.myapplication.repository.SolicitudRepository // ⬅️ Necesario
import co.edu.unipiloto.myapplication.storage.SessionManager
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardViewModel // ⬅️ Necesario
import co.edu.unipiloto.myapplication.viewmodel.ManagerDashboardViewModelFactory // ⬅️ Necesario
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
    private lateinit var viewModel: ManagerDashboardViewModel // 🌟 AGREGADO

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

        setupViewModel() // 🌟 Configurar ViewModel
        setupObservers() // 🌟 Configurar Observadores

        binding.recyclerViewBranchList.layoutManager = LinearLayoutManager(requireContext())

        adapter = SolicitudAdapter(
            items = emptyList(),
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                if (action == "ASIGNAR") {
                    handleAssignmentAction(solicitud)
                }
            }
        )

        binding.recyclerViewBranchList.adapter = adapter
        loadPendingRequests()
    }

    // --- NUEVOS MÉTODOS DE VIEWMODEL ---

    private fun setupViewModel() {
        // Inicialización manual (sin Dagger/Hilt)
        val repository = SolicitudRepository(RetrofitClient.getSolicitudApi())
        val factory = ManagerDashboardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ManagerDashboardViewModel::class.java]
    }

    private fun setupObservers() {
        // Observar el LiveData del ViewModel
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
        loadPendingRequests()
    }

    private fun loadPendingRequests() {

        val sucursalId = sessionManager.getSucursalId() ?: run {
            binding.tvBranchEmpty.visibility = View.VISIBLE
            binding.tvBranchEmpty.text = getString(R.string.error_no_branch_id)
            binding.recyclerViewBranchList.visibility = View.GONE
            return
        }

        // 🏆 CORRECCIÓN CLAVE: Usar el ViewModel para iniciar la carga con Corrutinas
        viewModel.loadBranchSolicitudes(sucursalId)
    }

    private fun handleAssignmentAction(solicitud: Solicitud) {

        val sucursalId = sessionManager.getSucursalId() ?: return

        // ❌ NOTA: Esta sección aún usa el patrón Call/enqueue y fallará si UserApi.getAvailableDriverBySucursal
        // fue migrada a suspend fun. Si funciona, mantente consistente con Call<T> aquí.
        RetrofitClient.getUserApi().getAvailableDriverBySucursal(sucursalId)
            .enqueue(object : Callback<User> {
                // ... (Lógica de onResponse y onFailure se mantiene) ...
                override fun onResponse(
                    call: Call<User>,
                    response: Response<User>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        assignRequest(solicitud.id!!, response.body()!!)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_collectors_available),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_network_driver_search),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun assignRequest(solicitudId: Long, recolector: User) {

        val body = mapOf("recolectorId" to recolector.id.toString())

        // ❌ NOTA: Esta sección aún usa el patrón Call/enqueue y fallará si SolicitudApi.assignRequest
        // fue migrada a suspend fun. Si funciona, mantente consistente con Call<T> aquí.
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