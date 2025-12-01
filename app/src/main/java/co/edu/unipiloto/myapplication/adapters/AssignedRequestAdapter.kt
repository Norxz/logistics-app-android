package co.edu.unipiloto.myapplication.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.SolicitudResponse

/**
 * Adaptador para mostrar la lista de Solicitudes (SolicitudResponse) en AssignDriverActivity.
 * @param onItemClicked Callback que se ejecuta cuando se hace clic en una solicitud.
 */
class AssignedRequestAdapter(
    private var requests: List<SolicitudResponse>,
    private val onItemClicked: (SolicitudResponse) -> Unit
) : RecyclerView.Adapter<AssignedRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 🚨 CORRECCIÓN: Eliminada la referencia incorrecta a clRequestItem como CardView
        val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
        val tvTrackingNumber: TextView = itemView.findViewById(R.id.tvTrackingNumber)
        val tvStatusLabel: TextView = itemView.findViewById(R.id.tvStatusLabel)
        val tvOriginAddress: TextView = itemView.findViewById(R.id.tvOriginAddress)
        val tvDestinationAddress: TextView = itemView.findViewById(R.id.tvDestinationAddress)
        val tvDriverName: TextView = itemView.findViewById(R.id.tvDriverName)
        val llDriverInfo: LinearLayout = itemView.findViewById(R.id.llDriverInfo)

        fun bind(request: SolicitudResponse) {
            tvTrackingNumber.text = "GUÍA #${request.guia.trackingNumber}"
            tvStatusLabel.text = request.estado.replace("_", " ")

            // Usamos los campos disponibles en SolicitudResponse
            tvOriginAddress.text = "Recolección: ${request.fechaRecoleccion} (${request.franjaHoraria})"
            tvDestinationAddress.text = "Entrega: ${request.direccionCompleta}"

            // Lógica para mostrar/ocultar información del Recolector (Conductor)
            // Se muestra si el recolectorId es válido y el nombre no está vacío
            if (request.recolectorId != null && request.recolectorId != 0L && !request.recolectorName.isNullOrBlank()) {
                llDriverInfo.visibility = View.VISIBLE
                tvDriverName.text = "${request.recolectorName} (ID: ${request.recolectorId})"
            } else {
                // Si no hay conductor asignado, mostramos el estado PENDIENTE y ocultamos el layout
                llDriverInfo.visibility = View.GONE
                tvDriverName.text = "PENDIENTE DE ASIGNACIÓN"
            }

            // Aplicar colores y estilos basados en el estado
            applyStatusStyling(request.estado)

            // 🏆 CORRECCIÓN 3: Listener en la vista raíz (CardView)
            itemView.setOnClickListener {
                onItemClicked(request)
            }
        }

        private fun applyStatusStyling(status: String) {
            val context = itemView.context

            // Definición de colores (Asegúrate que estos recursos existan en R.color)
            val colorResId = when (status.uppercase()) {
                "PENDIENTE_ASIGNACION" -> R.color.status_pending_assignment // Usar un color claro para pendiente
                "ASIGNADA" -> R.color.status_assigned
                "EN_RUTA", "EN_RECOLECCION" -> R.color.status_in_route
                "ENTREGADA", "COMPLETADA" -> R.color.status_delivered // Cambié a delivered por completed
                "CANCELADA" -> R.color.status_error
                else -> R.color.status_default
            }
            val color = ContextCompat.getColor(context, colorResId)

            // Aplicar color al indicador lateral
            statusIndicator.setBackgroundColor(color)

            // Aplicar color al fondo del TextView de estado
            tvStatusLabel.backgroundTintList = ColorStateList.valueOf(color)

            // Opcional: Cambiar el color del texto de la etiqueta de estado si el fondo es oscuro
            if (status.uppercase() == "PENDIENTE_ASIGNACION") {
                tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.on_surface_secondary))
            } else {
                tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.md_white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_status, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<SolicitudResponse>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}