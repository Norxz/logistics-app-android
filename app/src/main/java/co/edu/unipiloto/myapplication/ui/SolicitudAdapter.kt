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
 * Se encarga de vincular los datos de cada solicitud a una vista de ítem en la lista.
 * Incluye lógica para:
 * - Mostrar u ocultar un botón de cancelación según el estado de la solicitud.
 * - Colorear el texto del estado para una mejor retroalimentación visual.
 * - Delegar eventos de clic (cancelación) a la Actividad o Fragmento que lo contiene.
 * - Actualizar la lista de datos que se muestra.
 *
 * @property items La lista de [SolicitudItem] que se mostrará en el RecyclerView.
 * @property showCancelButton Un booleano que determina si el botón de cancelación debe ser visible.
 * @constructor Es privado para forzar la creación de instancias a través del método de fábrica `forCliente`.
 */
class SolicitudAdapter private constructor(
    // La lista se maneja internamente como mutable para poder actualizarla,
    // aunque el constructor reciba una lista inmutable.
    private var items: MutableList<SolicitudItem>,
    private val showCancelButton: Boolean
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    /**
     * Interfaz funcional para manejar el evento de clic en el botón de cancelar.
     * Recibe el ID de la solicitud y su posición en el adaptador.
     */
    private var onCancelListener: ((Long, Int) -> Unit)? = null

    /**
     * Establece el listener que será invocado cuando el usuario haga clic en el botón de cancelar de un ítem.
     *
     * @param listener La función lambda que se ejecutará, recibiendo el ID de la solicitud y su posición.
     */
    fun setOnCancelListener(listener: (solicitudId: Long, position: Int) -> Unit) {
        this.onCancelListener = listener
    }

    /**
     * Actualiza la lista de datos del adaptador y notifica al RecyclerView para que se redibuje.
     *
     * **Nota de implementación:** Esta es una solución simple que funciona para listas pequeñas.
     * Para aplicaciones en producción y listas grandes, se recomienda encarecidamente usar
     * `DiffUtil` para calcular las diferencias entre la lista vieja y la nueva. `DiffUtil`
     * actualiza solo los ítems que han cambiado, lo que resulta en animaciones más fluidas
     * y un rendimiento mucho mejor.
     *
     * @param newItems La nueva lista de [SolicitudItem] a mostrar.
     */
    fun updateData(newItems: List<SolicitudItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged() // Notifica que todos los datos han cambiado. Ineficiente para listas grandes.
    }


    companion object {
        /**
         * Método de fábrica para crear una instancia del adaptador configurada para la vista de un cliente.
         * En esta configuración, el botón de cancelar es visible por defecto.
         *
         * @param items La lista inicial de solicitudes a mostrar.
         * @return Una nueva instancia de [SolicitudAdapter].
         */
        fun forCliente(items: List<SolicitudItem>): SolicitudAdapter {
            // Se convierte la lista a mutable para que el método `updateData` pueda funcionar.
            return SolicitudAdapter(items.toMutableList(), showCancelButton = true)
        }

        /**
         * Método de fábrica para crear una instancia del adaptador configurada para el CONDUCTOR.
         */
        fun forConductor(items: List<SolicitudItem>): SolicitudAdapter {
            // Llama al constructor privado desde dentro de la clase (donde es accesible)
            return SolicitudAdapter(items.toMutableList(), showCancelButton = false)
        }
    }

    /**
     * Crea y devuelve un [SolicitudViewHolder] inflando el layout `R.layout.item_solicitud`.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

    /**
     * Vincula los datos de un [SolicitudItem] en una posición específica al [SolicitudViewHolder].
     * Gestiona la visibilidad del botón de cancelación y asigna el listener de clic.
     */
    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.btnCancelar.setOnClickListener {
            onCancelListener?.invoke(item.id, position)
        }

        // El botón de cancelar solo es visible si `showCancelButton` es true
        // y el estado no es final (ENTREGADA o CANCELADA).
        val isFinalized = item.estado.equals("ENTREGADA", ignoreCase = true) || item.estado.equals(
            "CANCELADA",
            ignoreCase = true
        )
        holder.btnCancelar.visibility =
            if (showCancelButton && !isFinalized) View.VISIBLE else View.GONE
    }

    /**
     * Devuelve el número total de ítems en la lista.
     */
    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder que representa la vista de un único ítem de solicitud.
     *
     * Su propósito es mantener en caché las referencias a las vistas (evitando costosas
     * llamadas a `findViewById` cada vez que se recicla) y proporcionar un método `bind`
     * para poblar los datos.
     *
     * @param itemView La vista raíz del ítem.
     */
    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTracking: TextView = itemView.findViewById(R.id.tvShipmentId)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvDate: TextView = itemView.findViewById(R.id.tvFecha)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelar)

        /**
         * Vincula los datos de un [SolicitudItem] a las vistas de este ViewHolder.
         *
         * @param item El objeto [SolicitudItem] que contiene los datos a mostrar.
         */
        fun bind(item: SolicitudItem) {
            tvTracking.text = itemView.context.getString(R.string.guide_example, item.id.toString())
            tvAddress.text = item.direccion
            tvDate.text = item.fecha
            tvStatus.text = item.estado.uppercase()

            // Determina el color del texto del estado según su valor para una retroalimentación visual clara.
            val colorRes = when (item.estado.uppercase()) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA", "EN_CAMINO" -> R.color.status_active
                "ENTREGADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_cancelled
                else -> android.R.color.darker_gray // Color de fallback seguro.
            }
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }
}
