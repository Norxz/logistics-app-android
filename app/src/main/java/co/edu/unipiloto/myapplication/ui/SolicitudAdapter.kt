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
     * Establece el listener que será invocado cuando el usuario haga clic en el botón de cancelar.
     */
    fun setOnCancelListener(listener: (solicitudId: Long, position: Int) -> Unit) {
        this.onCancelListener = listener
    }

    /**
     * Actualiza la lista de datos del adaptador y notifica al RecyclerView.
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
         * Crea una instancia del adaptador configurada para la vista de un **cliente**.
         * Muestra el botón de cancelar.
         */
        fun forCliente(items: List<SolicitudItem>): SolicitudAdapter {
            return SolicitudAdapter(items.toMutableList(), showCancelButton = true)
        }

        /**
         * Crea una instancia del adaptador configurada para el **conductor**.
         * Oculta el botón de cancelar, ya que el conductor tiene su propia lógica de botones.
         */
        fun forConductor(items: List<SolicitudItem>): SolicitudAdapter {
            return SolicitudAdapter(items.toMutableList(), showCancelButton = false)
        }
    }

    // -----------------------------------------------------
    // MÉTODOS DEL RECYCLERVIEW
    // -----------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        // Asumiendo que R.layout.item_solicitud existe.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

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

    override fun getItemCount(): Int = items.size

    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Mapeo de vistas de item_solicitud.xml
        private val tvTracking: TextView = itemView.findViewById(R.id.tvShipmentId)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvDate: TextView = itemView.findViewById(R.id.tvFecha)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelar)

        /**
         * Vincula los datos de un [SolicitudItem] a las vistas.
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