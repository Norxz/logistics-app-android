package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.databinding.FragmentBranchListBinding
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.User
import co.edu.unipiloto.myapplication.storage.SessionManager
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

        // ✅ Corregido: Usar SolicitudResponse consistentemente
        RetrofitClient.getSolicitudApi().getSolicitudesBySucursal(sucursalId)
            .enqueue(object : Callback<List<SolicitudResponse>> {

                // ✅ FIX: El tipo del Call y Response debe ser List<SolicitudResponse>
                override fun onResponse(
                    call: Call<List<SolicitudResponse>>,
                    response: Response<List<SolicitudResponse>>
                ) {
                    if (response.isSuccessful) {

                        // **NOTA IMPORTANTE:** Si usas SolicitudResponse, debes convertirlo a Solicitud si el adaptador lo requiere.
                        // Asumiendo que SolicitudResponse es el modelo Solicitud:
                        @Suppress("UNCHECKED_CAST")
                        val solicitudes = response.body()?.filter { it.estado == "PENDIENTE" }
                                as? List<Solicitud> ?: emptyList()

                        if (solicitudes.isNotEmpty()) {
                            binding.tvBranchEmpty.visibility = View.GONE
                            binding.recyclerViewBranchList.visibility = View.VISIBLE
                            adapter.updateData(solicitudes)
                        } else {
                            binding.tvBranchEmpty.visibility = View.VISIBLE
                            binding.recyclerViewBranchList.visibility = View.GONE
                            binding.tvBranchEmpty.text = getString(R.string.no_pending_requests_branch)
                        }

                    } else {
                        Log.e("BranchPending", "Error ${response.code()} al cargar solicitudes.")
                        binding.tvBranchEmpty.visibility = View.VISIBLE
                        binding.tvBranchEmpty.text = getString(R.string.error_server_code, response.code().toString())
                    }
                }

                // ✅ FIX: El tipo del Call debe ser List<SolicitudResponse>
                override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                    Log.e("BranchPending", "Fallo de red: ${t.message}")
                    binding.tvBranchEmpty.visibility = View.VISIBLE
                    binding.tvBranchEmpty.text = getString(R.string.error_network_fail)
                }
            })
    }

    private fun handleAssignmentAction(solicitud: Solicitud) {

        val sucursalId = sessionManager.getSucursalId() ?: return

        // ❌ ERROR PENDIENTE: 'getAvailableDriverBySucursal' no está definido en UserApi.
        RetrofitClient.getUserApi().getAvailableDriverBySucursal(sucursalId)
            .enqueue(object : Callback<User> {

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

        // ✅ FIX: Usar SolicitudResponse consistentemente
        RetrofitClient.getSolicitudApi().assignRequest(solicitudId, body)
            .enqueue(object : Callback<SolicitudResponse> {

                // ✅ FIX: El tipo del Call y Response debe ser SolicitudResponse
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
                        loadPendingRequests()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_assignment_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // ✅ FIX: El tipo del Call debe ser SolicitudResponse
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