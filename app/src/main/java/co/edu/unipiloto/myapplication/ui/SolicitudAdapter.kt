package co.edu.unipiloto.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository.SolicitudItem

/**
 * Adaptador para un [RecyclerView] que muestra una lista de objetos [SolicitudItem].
 *
 * Mapea la vista item_solicitud.xml y se usa principalmente en el Dashboard del Cliente (MainActivity).
 * Incluye lógica para mostrar/ocultar el botón de cancelación.
 *
 * @property items La lista de [SolicitudItem] que se mostrará en el RecyclerView.
 * @property showCancelButton Un booleano que determina si el botón de cancelación debe ser visible.
 * @constructor Es privado para forzar la creación de instancias a través del método de fábrica `forCliente`.
 */
class SolicitudAdapter private constructor(
    // La lista se maneja internamente como mutable para poder actualizarla
    private var items: MutableList<SolicitudItem>,
    private val showCancelButton: Boolean
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    private var onCancelListener: ((Long, Int) -> Unit)? = null

    /**
     * Set a callback invoked when the cancel button is clicked for an item.
     *
     * @param listener Function called with the cancelled item's `solicitudId` and its adapter `position`.
     */
    fun setOnCancelListener(listener: (solicitudId: Long, position: Int) -> Unit) {
        this.onCancelListener = listener
    }

    /**
     * Replace the adapter's items with the given list and refresh the RecyclerView.
     *
     * @param newItems The new list of SolicitudItem to display; existing items are removed and replaced.
     */
    fun updateData(newItems: List<SolicitudItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // -----------------------------------------------------
    // MÉTODOS DE FÁBRICA
    // -----------------------------------------------------
    companion object {
        /**
         * Create an adapter instance configured for a client view with the cancel button visible.
         *
         * @param items Initial list of SolicitudItem to display.
         * @return A SolicitudAdapter configured to show the cancel button and initialized with the provided items.
         */
        fun forCliente(items: List<SolicitudItem>): SolicitudAdapter {
            return SolicitudAdapter(items.toMutableList(), showCancelButton = true)
        }

        /**
         * Create an adapter configured for the driver view with the cancel button hidden.
         *
         * @return A SolicitudAdapter configured for drivers (cancel button not shown).
         */
        fun forConductor(items: List<SolicitudItem>): SolicitudAdapter {
            return SolicitudAdapter(items.toMutableList(), showCancelButton = false)
        }
    }

    // -----------------------------------------------------
    // MÉTODOS DEL RECYCLERVIEW
    /**
     * Inflates the item_solicitud layout and returns a new SolicitudViewHolder.
     *
     * @param parent The parent ViewGroup used to inflate the view.
     * @param viewType The view type for the new view (unused).
     * @return A SolicitudViewHolder backed by the inflated item_solicitud view.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        // Asumiendo que R.layout.item_solicitud existe.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

    /**
     * Binds the item at the given position to the holder and configures cancel behaviour and visibility.
     *
     * Sets the view data via holder.bind(item), assigns the cancel button click to invoke the adapter's
     * cancel listener with the item's id and position, and makes the cancel button visible only when
     * the adapter is configured to show it and the item's estado is not "ENTREGADA" or "CANCELADA".
     *
     * @param holder ViewHolder that will be bound with the item at [position].
     * @param position Index of the item in the adapter's list to bind.
     */
    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.btnCancelar.setOnClickListener {
            onCancelListener?.invoke(item.id, position)
        }

        // Lógica para mostrar/ocultar el botón de cancelar
        val isFinalized = item.estado.equals("ENTREGADA", ignoreCase = true) || item.estado.equals(
            "CANCELADA",
            ignoreCase = true
        )
        // Solo muestra el botón si el adaptador está configurado para mostrarlo Y el estado no es final.
        holder.btnCancelar.visibility =
            if (showCancelButton && !isFinalized) View.VISIBLE else View.GONE
    }

    /**
 * Get the number of items managed by the adapter.
 *
 * @return The number of items in the adapter.
 */
override fun getItemCount(): Int = items.size

    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Mapeo de vistas de item_solicitud.xml
        private val tvTracking: TextView = itemView.findViewById(R.id.tvShipmentId)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvDate: TextView = itemView.findViewById(R.id.tvFecha)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelar)

        /**
         * Binds a SolicitudItem's data to the ViewHolder's views.
         *
         * Updates tracking id, address, date label, and status text, and applies a color to the status based on its value.
         *
         * @param item The SolicitudItem whose values will populate the views.
         */
        fun bind(item: SolicitudItem) {
            val context = itemView.context

            // Usamos R.string.guide_example (Guía: #%1$s)
            tvTracking.text = context.getString(R.string.guide_example, item.id.toString())
            tvAddress.text = item.direccion
            tvDate.text = context.getString(R.string.delivery_adress) + ": " + item.fecha // Adaptamos la fecha para mostrar un texto descriptivo
            tvStatus.text = item.estado.uppercase()

            // Lógica de color según el estado
            val colorRes = when (item.estado.uppercase()) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA", "EN_CAMINO", "EN RUTA" -> R.color.status_active // Usamos status_active
                "ENTREGADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_cancelled
                else -> R.color.status_default
            }
            tvStatus.setTextColor(ContextCompat.getColor(context, colorRes))

        }
    }
}