package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
// Importamos el adaptador genérico que hemos desarrollado
import co.edu.unipiloto.myapplication.adapters.SolicitudAdapter
import co.edu.unipiloto.myapplication.db.SolicitudRepository
// Importamos el modelo de datos para la inferencia de tipo
import co.edu.unipiloto.myapplication.models.Solicitud
import co.edu.unipiloto.myapplication.storage.SessionManager

/**
 * Fragmento que muestra un historial de solicitudes completadas (ENTREGADA/CANCELADA)
 * dentro de la zona logística del Gestor/Funcionario.
 */
class BranchCompletedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var solicitudRepository: SolicitudRepository
    private lateinit var sessionManager: SessionManager

    // Usaremos el SolicitudAdapter genérico (BranchSolicitudAdapter debe ser un alias o este mismo)
    // Cambiamos el tipo a SolicitudAdapter
    private lateinit var adapter: SolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar repositorios
        solicitudRepository = SolicitudRepository(requireContext())
        sessionManager = SessionManager(requireContext())

        // Mapear vistas
        recyclerView = view.findViewById(R.id.recyclerViewBranchList)
        tvEmpty = view.findViewById(R.id.tvBranchEmpty)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🏆 CORRECCIÓN DE ERROR: Inicializar SolicitudAdapter especificando el tipo de lista vacía.
        adapter = SolicitudAdapter(
            // Especificamos explicitamente que es una lista de Solicitud
            items = emptyList<Solicitud>(),
            // Pasamos el rol para que el adaptador sepa qué mostrar (ej. ningún botón)
            role = sessionManager.getRole() ?: "GESTOR",
            onActionClick = { solicitud, action ->
                // En el historial, no se esperan acciones, solo quizás ver detalles.
                Log.d("CompletedFrag", "Acción: $action en historial ${solicitud.id}. No se procesa.")
            }
        )
        recyclerView.adapter = adapter

        loadCompletedRequests()
    }

    override fun onResume() {
        super.onResume()
        // Aseguramos que se recargan los datos cada vez que el fragmento se hace visible
        loadCompletedRequests()
    }


    /**
     * Carga las solicitudes que ya están en estado 'ENTREGADA', 'CANCELADA', o 'FINALIZADA'
     * para la zona del gestor.
     */
    private fun loadCompletedRequests() {
        val zona = sessionManager.getZona() ?: run {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.error_no_zone)
            recyclerView.visibility = View.GONE
            return
        }

        // Usamos el método correcto del repositorio que devuelve el modelo enriquecido
        val completedItems = solicitudRepository.getSolicitudesFinalizadasEnriquecidasPorZona(zona)

        if (completedItems.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(completedItems) // Actualiza el adaptador
        } else {
            // Muestra mensaje si no hay solicitudes completadas
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            // Deberías agregar este string a tu strings.xml:
            tvEmpty.text = getString(R.string.no_completed_requests)
        }
    }
}