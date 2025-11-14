package co.edu.unipiloto.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.models.Solicitud // Assuming Solicitud is updated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Adaptador ÚNICO y GENÉRICO para la lista de solicitudes, maneja la lógica de visualización
 * y las acciones de botones según el rol (Cliente, Conductor, Gestor).
 *
 * @param items Lista de objetos [Solicitud].
 * @param role Rol del usuario actual ("CLIENTE", "CONDUCTOR", "GESTOR").
 * @param onActionClick Callback que se ejecuta al presionar un botón de acción.
 */
class SolicitudAdapter(
    private var items: List<Solicitud>,
    private val role: String,
    private val onActionClick: ((Solicitud, String) -> Unit)? = null // Solicitud y Tipo de Acción (ej. "INICIAR_RECOLECCION")
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    fun updateData(newItems: List<Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        // Asumiendo que item_solicitud.xml es el layout que contiene todos los campos y botones.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        holder.bind(items[position], role, onActionClick)
    }

    override fun getItemCount(): Int = items.size

    class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Mapeo de vistas del XML item_solicitud.xml
        private val tvSolicitudID: TextView = itemView.findViewById(R.id.tvShipmentId) // Usando tu ID
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvCreado: TextView = itemView.findViewById(R.id.tvCreado)

        // Botones de Acción (Asumimos que solo necesitamos uno o dos botones visibles a la vez)
        private val btnPrimaryAction: Button = itemView.findViewById(R.id.btnAceptar) // Reutilizamos Aceptar como acción principal
        private val btnSecondaryAction: Button = itemView.findViewById(R.id.btnCancelar) // Reutilizamos Cancelar para acciones secundarias

        // Ocultamos los botones específicos del placeholder XML para usar solo dos
        private val btnEnCamino: Button = itemView.findViewById(R.id.btnEnCamino)
        private val btnEntregado: Button = itemView.findViewById(R.id.btnEntregado)
        private val btnConfirmar: Button = itemView.findViewById(R.id.btnConfirmar)


        fun bind(solicitud: Solicitud, role: String, onActionClick: ((Solicitud, String) -> Unit)?) {

            // --- 1. SETEAR DATOS ---
            tvSolicitudID.text = itemView.context.getString(R.string.guide_example, solicitud.id.toString())
            tvEstado.text = solicitud.estado.replace("_", " ").uppercase()

            // CORRECTED: Access properties directly from the non-nullable 'direccion' object
            val direccionCompleta = solicitud.direccion?.direccionCompleta ?: "Dirección No Registrada"
            val ciudad = solicitud.direccion?.ciudad ?: "N/D"

            tvDireccion.text = itemView.context.getString(
                R.string.full_address_format,
                direccionCompleta,
                ciudad
            )

            tvFecha.text = itemView.context.getString(
                R.string.collection_time_format,
                solicitud.fechaRecoleccion,
                solicitud.franjaHoraria
            )

            // Asumiendo que createdAt es un Instant (serializado a String)
            tvCreado.text = itemView.context.getString(
                R.string.created_at_format,
                formatInstantToDate(solicitud.createdAt)
            )

            // --- 2. GESTIONAR VISIBILIDAD DE BOTONES ---
            hideAllButtons()
            setupButtonsByRoleAndState(solicitud, role, onActionClick)

            // --- 3. COLOR DEL ESTADO ---
            setEstadoColor(itemView.context, solicitud.estado.uppercase(), tvEstado)
        }

        private fun formatInstantToDate(instantString: String?): String {
            if (instantString.isNullOrEmpty()) {
                return "Fecha/Hora Desconocida"
            }

            return try {
                // 1. Limpieza de microsegundos y Z
                // Tomamos la parte antes de la 'Z' y truncamos a 3 milisegundos si existen.
                val basePart = instantString.substringBefore('Z').substringBeforeLast('.')
                val millisPart = instantString.substringAfterLast('.').substringBefore('Z').take(3)

                val cleanedString = "${basePart}.${millisPart}Z"

                // 2. Definición del Formato de Entrada ISO (patrón fijo)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC") // La entrada siempre es UTC

                // 3. Definición del Formato de Salida
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getDefault() // Convertir a zona local

                val date = inputFormat.parse(cleanedString)
                return outputFormat.format(date!!)

            } catch (e: Exception) {
                // Si falla, devuelve una versión simplificada del string (solo fecha y hora)
                Log.e("Adapter", "Fallo de parsing '$instantString': ${e.message}")
                return instantString.substringBefore("T") + " (Error Formato)"
            }
        }

        private fun hideAllButtons() {
            btnPrimaryAction.visibility = View.GONE
            btnSecondaryAction.visibility = View.GONE
            // Ocultar los botones no utilizados del layout para limpieza
            btnEnCamino.visibility = View.GONE
            btnEntregado.visibility = View.GONE
            btnConfirmar.visibility = View.GONE
        }


        private fun setupButtonsByRoleAndState(solicitud: Solicitud, role: String, onActionClick: ((Solicitud, String) -> Unit)?) {
            // Utilizamos el patrón de acción/siguiente estado
            val (primaryText, primaryAction, secondaryText, secondaryAction) = getActionDetails(solicitud.estado.uppercase(), role)

            if (primaryAction != null) {
                btnPrimaryAction.visibility = View.VISIBLE
                btnPrimaryAction.text = primaryText
                btnPrimaryAction.setOnClickListener { onActionClick?.invoke(solicitud, primaryAction) }
            }

            if (secondaryAction != null) {
                btnSecondaryAction.visibility = View.VISIBLE
                btnSecondaryAction.text = secondaryText

                if (secondaryAction == "CANCELAR_CLIENTE") {
                    btnSecondaryAction.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_error))
                }

                btnSecondaryAction.setOnClickListener { onActionClick?.invoke(solicitud, secondaryAction) }
            }
        }

        /**
         * Define las acciones y textos de los botones según el estado y el rol.
         * Retorna: (Texto Principal, Acción Principal, Texto Secundario, Acción Secundaria)
         */
        private fun getActionDetails(estado: String, role: String): Quad<String, String?, String, String?> {
            return when (role.uppercase()) {
                "CLIENTE" -> when (estado) {
                    "PENDIENTE" -> Quad("", null, "CANCELAR", "CANCELAR_CLIENTE")
                    "ENTREGADA" -> Quad("CONFIRMAR RECEPCIÓN", "CONFIRMAR_ENTREGA", "", null)
                    else -> Quad("", null, "", null)
                }
                "CONDUCTOR" -> when (estado) {
                    "ASIGNADA" -> Quad("INICIAR RECOLECCIÓN", "EN_RECOLECCION", "", null)
                    "EN_RECOLECCION" -> Quad("MARCAR RECOGIDA", "RECOGIDA", "", null)
                    "RECOGIDA" -> Quad("MARCAR ENTREGADA", "ENTREGADA", "", null)
                    else -> Quad("", null, "", null)
                }
                "GESTOR" -> when (estado) {
                    "PENDIENTE" -> Quad("ASIGNAR RECOLECTOR", "ASIGNAR", "", null) // Usado en BranchPendingFragment
                    else -> Quad("", null, "", null) // Los gestores no suelen tener más acciones directas aquí
                }
                else -> Quad("", null, "", null)
            }
        }


        /**
         * Asigna un color al texto de estado basado en el estado de la solicitud.
         */
        private fun setEstadoColor(context: Context, estado: String, tvStatus: TextView) {
            val colorRes = when (estado) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA", "EN_RECOLECCION" -> R.color.status_in_route
                "RECOGIDA" -> R.color.status_in_transit
                "ENTREGADA", "FINALIZADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_cancelled
                else -> R.color.status_default
            }
            tvStatus.setTextColor(ContextCompat.getColor(context, colorRes))
        }


        // Clase de datos auxiliar para retornar cuatro valores
        data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    }
}