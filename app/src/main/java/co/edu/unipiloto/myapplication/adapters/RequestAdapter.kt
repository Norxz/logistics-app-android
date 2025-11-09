package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R // Asegúrate de que esta importación sea correcta para R
import co.edu.unipiloto.myapplication.models.Request
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar la lista de solicitudes en el RecyclerView.
class RequestAdapter(
    private val requests: MutableList<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    // ----------------------------------------------------------
    // 1. ViewHolder (Víncula la vista del ítem a los datos)
    // ----------------------------------------------------------
    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGuiaID: TextView = view.findViewById(R.id.tvGuiaID)
        val tvRequestStatus: TextView = view.findViewById(R.id.tvRequestStatus)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvClientName: TextView = view.findViewById(R.id.tvClientName)
        val tvCreationDate: TextView = view.findViewById(R.id.tvCreationDate)
        val btnViewDetails: MaterialButton = view.findViewById(R.id.btnViewDetails)

        fun bind(request: Request) {
            tvGuiaID.text = "GUÍA #${request.guiaId}"
            tvAddress.text = request.address
            tvClientName.text = "Cliente: ${request.clientName}"

            // Formateo de la fecha (Asume que creationTimestamp es un String tipo ISO)
            tvCreationDate.text = formatTimestamp(request.creationTimestamp)

            // Asigna el estado y estiliza el badge
            setStatusAndStyle(request.status, request.assignedRecolectorName)

            // Manejador del clic del botón GESTIONAR
            btnViewDetails.setOnClickListener {
                onItemClick(request)
            }
        }

        /**
         * Establece el texto y el estilo (color de fondo y texto) del badge de estado.
         * Requiere que existan drawables de forma (shape) para cada estado en tu carpeta `res/drawable`.
         */
        private fun setStatusAndStyle(status: String, recolectorName: String?) {
            // Mapeo de estados para la UI
            val displayStatus: String
            val backgroundDrawableId: Int
            val textColorId: Int

            when (status.uppercase(Locale.ROOT)) {
                "PENDIENTE" -> {
                    displayStatus = "PENDIENTE"
                    backgroundDrawableId = R.drawable.bg_status_pending // Asume un drawable de color Amarillo/Naranja
                    textColorId = R.color.md_white
                }
                "ASIGNADA" -> {
                    displayStatus = "ASIGNADA a ${recolectorName ?: "Conductor"}"
                    backgroundDrawableId = R.drawable.bg_status_assigned // Asume un drawable de color Azul
                    textColorId = R.color.md_white
                }
                "EN_RUTA" -> {
                    displayStatus = "EN RUTA"
                    backgroundDrawableId = R.drawable.bg_status_in_route // Asume un drawable de color Azul Oscuro
                    textColorId = R.color.md_white
                }
                "COMPLETADA" -> {
                    displayStatus = "COMPLETADA"
                    backgroundDrawableId = R.drawable.bg_status_completed // Asume un drawable de color Verde
                    textColorId = R.color.md_white
                }
                else -> {
                    displayStatus = status
                    backgroundDrawableId = R.drawable.bg_status_default
                    textColorId = R.color.on_surface
                }
            }

            tvRequestStatus.text = displayStatus
            tvRequestStatus.background = ContextCompat.getDrawable(itemView.context, backgroundDrawableId)
            tvRequestStatus.setTextColor(ContextCompat.getColor(itemView.context, textColorId))
        }

        /**
         * Formatea la marca de tiempo de la base de datos a un formato legible por humanos.
         * Asume que el formato de la BD es ISO 8601 (yyyy-MM-dd HH:mm:ss o similar).
         */
        private fun formatTimestamp(timestamp: String): String {
            return try {
                // Formato de entrada de la BD (ej. 2025-11-08 19:30:00)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
                val date = inputFormat.parse(timestamp)

                // Formato de salida deseado (ej. Creada el: 08/Nov/2025 19:30)
                val outputFormat = SimpleDateFormat("Creada el: dd/MMM/yyyy HH:mm", Locale("es", "CO"))
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Fecha inválida: $timestamp"
            }
        }
    }

    // ----------------------------------------------------------
    // 2. Implementación de los métodos del Adaptador
    // ----------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_list, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size
}