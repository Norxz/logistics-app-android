// co.edu.unipiloto.myapplication.adapters.RequestAdapter.kt
package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.model.Solicitud // Importamos el modelo correcto

/**
 * Adaptador para mostrar una lista de Solicitudes en un RecyclerView.
 *
 * NOTA: Asumo que tienes un layout llamado 'item_request' con tvGuiaId, tvClientName y tvStatus.
 */
class RequestAdapter(
    private val requestList: MutableList<Solicitud>,
    private val clickListener: (Solicitud) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    // Método de extensión para asegurar que RecyclerView.Adapter tenga notifyDataSetChanged()
    // Ya está implementado por defecto, pero lo mantenemos simple.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false) // 🚨 ASEGÚRATE DE TENER ESTE LAYOUT
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val solicitud = requestList[position]
        holder.bind(solicitud, clickListener)
    }

    override fun getItemCount(): Int = requestList.size

    // Actualiza la lista y notifica los cambios (usado en loadRequests)
    fun updateData(newRequests: List<Solicitud>) {
        requestList.clear()
        requestList.addAll(newRequests)
        notifyDataSetChanged()
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ CORREGIDO: Usar tvGuiaID (mayúsculas)
        private val tvGuiaID: TextView = itemView.findViewById(R.id.tvGuiaID)

        private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)

        // ✅ CORREGIDO: Usar tvRequestStatus
        private val tvRequestStatus: TextView = itemView.findViewById(R.id.tvRequestStatus)

        fun bind(solicitud: Solicitud, clickListener: (Solicitud) -> Unit) {
            // ✅ ACCESO CORREGIDO: Usar las variables corregidas
            tvGuiaID.text = "Guía: #${solicitud.guia.id}"
            tvClientName.text = solicitud.client.fullName
            tvRequestStatus.text = solicitud.estado // Usar la variable corregida

            itemView.setOnClickListener {
                clickListener(solicitud)
            }
        }
    }
}