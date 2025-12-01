package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.model.Solicitud

/**
 * 📦 Adaptador para mostrar una lista de [Solicitud]es en un RecyclerView.
 *
 * * CORRECCIÓN: Se actualiza el manejo de los estados para incluir todos los valores
 * del Enum [EstadoSolicitud] del backend.
 */
class RequestAdapter(
    private val requestList: MutableList<Solicitud>,
    private val clickListener: (Solicitud) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val solicitud = requestList[position]
        holder.bind(solicitud, clickListener)
    }

    override fun getItemCount(): Int = requestList.size

    /**
     * 🔄 Actualiza la lista de datos del adaptador y notifica al RecyclerView.
     */
    fun updateData(newRequests: List<Solicitud>) {
        requestList.clear()
        requestList.addAll(newRequests)
        notifyDataSetChanged()
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // --- Vistas obligatorias ---
        private val tvGuiaID: TextView = itemView.findViewById(R.id.tvGuiaID)
        private val tvRequestStatus: TextView = itemView.findViewById(R.id.tvRequestStatus)
        private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)

        // --- Vistas Faltantes ---
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvCreationDate: TextView = itemView.findViewById(R.id.tvCreationDate)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)

        /**
         * Enlaza una entidad [Solicitud] a los [TextView]s del ViewHolder.
         */
        fun bind(solicitud: Solicitud, clickListener: (Solicitud) -> Unit) {

            // 1. Título y Estado
            val trackingNumber = solicitud.guia.trackingNumber ?: "N/D"
            tvGuiaID.text = "Tracking: #$trackingNumber"
            tvRequestStatus.text = solicitud.estado

            // 2. Dirección de Entrega
            val direccionCompleta = solicitud.direccionEntrega.direccionCompleta
            tvAddress.text = "📍 $direccionCompleta"

            // 3. Nombre del Cliente / Conductor Asignado
            val conductorNombre = solicitud.conductor?.fullName ?: solicitud.conductor?.fullName

            val clientDisplay = if (!conductorNombre.isNullOrBlank()) {
                "🚚 Conductor: $conductorNombre"
            } else {
                val remitenteNombre = solicitud.remitente?.nombre ?: solicitud.remitente?.nombre
                "👤 Cliente: ${remitenteNombre ?: "ID #${solicitud.client.id}"}"
            }
            tvClientName.text = clientDisplay

            // 4. Fecha de Creación
            val fechaCorta = solicitud.createdAt.split("T").firstOrNull() ?: "Fecha desconocida"
            tvCreationDate.text = "🗓️ Creada el: $fechaCorta"

            // 5. Listener para el Botón
            val listener = { clickListener(solicitud) }
            itemView.setOnClickListener { listener() }
            btnViewDetails.setOnClickListener { listener() }

            // 6. Manejo de colores de estado (Actualizado para los nuevos estados)
            val colorResId = when (solicitud.estado) {
                // Estados de espera/iniciales
                "PENDIENTE" -> R.drawable.bg_status_pending
                "ASIGNADA" -> R.drawable.bg_status_assigned

                // Estados de movimiento (Ruta o Tránsito)
                "EN_RUTA_RECOLECCION", "EN_RUTA_REPARTO" -> R.drawable.bg_status_in_route // Asumimos un color para ambas rutas
                "EN_DISTRIBUCION" -> R.drawable.bg_status_in_transit // Asumimos un color para el tránsito

                // Estados Finales
                "ENTREGADA" -> R.drawable.bg_status_delivered // Verde
                "CANCELADA" -> R.drawable.bg_status_cancelled // Rojo

                // Estado por defecto o desconocido
                else -> R.drawable.bg_status_pending
            }
            tvRequestStatus.setBackgroundResource(colorResId)
        }
    }
}