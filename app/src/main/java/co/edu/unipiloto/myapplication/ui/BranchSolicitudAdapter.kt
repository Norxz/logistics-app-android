package co.edu.unipiloto.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository

/**
 * Adaptador para mostrar la lista de solicitudes en el dashboard del Funcionario de Sucursal
 * y el Dashboard del Gestor (pestaña Asignadas).
 * Mapea al layout item_solicitud_branch.xml.
 */
class BranchSolicitudAdapter(
    private var items: List<SolicitudRepository.SolicitudItem>
) : RecyclerView.Adapter<BranchSolicitudAdapter.ViewHolder>() {

    /**
     * Replace the adapter's items with the given list and refresh the RecyclerView display.
     *
     * @param newItems The new list of SolicitudRepository.SolicitudItem to use as the adapter's data set.
     */
    fun updateData(newItems: List<SolicitudRepository.SolicitudItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Inflates the branch solicitud item layout and returns a ViewHolder for it.
     *
     * @param parent The parent ViewGroup used to inflate the item layout.
     * @param viewType The view type of the new View (unused).
     * @return A ViewHolder initialized with the inflated item_solicitud_branch view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Usamos el layout del ítem de sucursal
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud_branch, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the adapter item at the given position to the provided ViewHolder.
     *
     * @param holder The ViewHolder into which the item should be bound.
     * @param position The index of the item in the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    /**
 * Get the number of items in the adapter's data set.
 *
 * @return The number of items currently held by the adapter.
 */
override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Mapeo de vistas de item_solicitud_branch.xml
        private val tvSolicitudID: TextView = itemView.findViewById(R.id.tvBranchSolicitudID)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvBranchDestination)
        private val tvClientName: TextView = itemView.findViewById(R.id.tvBranchClientName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvBranchStatus)

        /**
         * Binds a SolicitudItem's data to the ViewHolder's views.
         *
         * Sets the displayed request ID, destination address, a client/date placeholder, and the status text.
         * Applies a status-dependent text color mapped from the item's estado value.
         *
         * @param item The SolicitudItem whose data will populate the view fields.
         */
        fun bind(item: SolicitudRepository.SolicitudItem) {
            val context = itemView.context

            // 1. ID de la Solicitud (Guía)
            tvSolicitudID.text = context.getString(R.string.guide_example, item.id.toString())

            // 2. Dirección de Destino
            tvDestination.text = context.getString(R.string.delivery_adress) + ": " + item.direccion

            // 3. Nombre del Cliente/Remitente (Se asume que la consulta original trae el nombre)
            // Ya que SolicitudItem solo tiene la dirección, mostramos un placeholder o más detalles de la dirección.
            // Si necesitaras el nombre real, la Activity debería consultarlo en UserRepository.
            tvClientName.text = "Fecha: ${item.fecha}"

            // 4. Estado y Color
            tvStatus.text = item.estado.uppercase()

            val colorRes = when (item.estado.uppercase()) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA", "EN_RUTA", "EN CAMINO" -> R.color.status_in_route
                "ENTREGADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_cancelled
                else -> R.color.status_default
            }
            tvStatus.setTextColor(ContextCompat.getColor(context, colorRes))
        }
    }
}