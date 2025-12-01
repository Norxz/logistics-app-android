package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.SolicitudResponse

class DriverRequestAdapter(
    private var requests: List<SolicitudResponse>,
    private val listener: OnRequestClickListener
) : RecyclerView.Adapter<DriverRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Vistas mapeadas a tu item_solicitud_driver.xml
        private val tvActionType: TextView = itemView.findViewById(R.id.tvActionType)
        private val tvGuiaID: TextView = itemView.findViewById(R.id.tvGuiaID)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
        private val btnStartTask: Button = itemView.findViewById(R.id.btnStartTask)

        fun bind(request: SolicitudResponse) {

            // --- 1. Mapeo de Datos ---
            tvGuiaID.text = itemView.context.getString(R.string.guia_id_format, request.guia.trackingNumber)
            tvAddress.text = request.direccionCompleta

            // CORRECCIÓN: Se usa 'nombreReceptor' en lugar de 'receptorName'
            val recipientName = "Destinatario: " + request.direccionCompleta

            tvClientName.text = itemView.context.getString(R.string.driver_recipient_format, recipientName)

            // --- 2. Lógica de Estados y Botón ---
            val (actionText, nextStatus, colorRes) = getActionDetails(request.estado)

            tvActionType.text = actionText.uppercase()

            if (nextStatus != null) {
                btnStartTask.text = nextStatus.uppercase()
                btnStartTask.visibility = View.VISIBLE

                // CORRECCIÓN: Se usa el color dinámico, asumiendo que R.color.status_collected está definido.
                btnStartTask.setBackgroundColor(ContextCompat.getColor(itemView.context, colorRes))

                // Configurar el listener
                btnStartTask.setOnClickListener {
                    listener.onRequestStatusChange(request.id, request.estado)
                }
            } else {
                btnStartTask.visibility = View.GONE
            }
        }
    }

    /**
     * Define el texto de la acción, el texto del siguiente estado en el botón, y el color.
     */
    private fun getActionDetails(currentStatus: String): Triple<String, String?, Int> {
        return when (currentStatus) {
            "ASIGNADA" -> Triple(
                "RECOGIDA PENDIENTE",
                "INICIAR RECOLECCIÓN",
                R.color.status_in_route
            )
            "EN_RUTA_RECOLECCION" -> Triple(
                "EN RECOLECCIÓN",
                "PAQUETE RECOLECTADO",
                R.color.status_collected // Color asumido
            )
            "EN_DISTRIBUCION" -> Triple(
                "EN ALMACÉN/DISTRIBUCIÓN",
                "INICIAR REPARTO FINAL",
                R.color.status_in_route
            )
            "EN_RUTA_REPARTO" -> Triple(
                "EN RUTA DE REPARTO",
                "FINALIZAR ENTREGA",
                R.color.status_delivered
            )
            "ENTREGADA" -> Triple("ENTREGADA", null, R.color.status_delivered)
            "CANCELADA" -> Triple("CANCELADA", null, R.color.status_cancelled)
            else -> Triple(currentStatus.replace("_", " "), null, R.color.on_surface_secondary)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud_driver, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<SolicitudResponse>) {
        requests = newRequests.filter { it.estado != "ENTREGADA" && it.estado != "CANCELADA" }
        notifyDataSetChanged()
    }
}