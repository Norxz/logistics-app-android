// Archivo: SolicitudAdapter.kt
package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.databinding.ItemSolicitudBinding
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.Sucursal
import co.edu.unipiloto.myapplication.model.User

/**
 * Define el tipo para el listener de clic en las acciones.
 * Recibe el modelo de Solicitud y la acción (String) realizada.
 */
typealias OnSolicitudActionListener = (Solicitud, String) -> Unit

class SolicitudAdapter(
    private var items: List<Solicitud>,
    private val role: String,
    private val onActionClick: OnSolicitudActionListener
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    // --- ViewHolder ---

    inner class SolicitudViewHolder(private val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: Solicitud) {
            val context = itemView.context

            // 1. Mostrar Datos Principales
            binding.tvSolicitudId.text = context.getString(R.string.solicitud_id_format, solicitud.id ?: 0L)
            binding.tvDireccion.text = solicitud.direccionEntrega.direccionCompleta
            binding.tvEstado.text = solicitud.estado

            // 2. Mostrar Conductor/Sucursal (Información Adicional)
            val infoExtra = when {
                // ✅ FIX: Usar 'solicitud.conductor' en lugar de 'solicitud.recolector'
                solicitud.conductor != null -> context.getString(R.string.recolector_assigned_format, solicitud.conductor.fullName)
                solicitud.sucursal != null -> context.getString(R.string.branch_origin_format, solicitud.sucursal.nombre)
                else -> ""
            }
            binding.tvInfoExtra.text = infoExtra
            binding.tvInfoExtra.visibility = if (infoExtra.isNotEmpty()) View.VISIBLE else View.GONE

            // Si el layout tiene tvFranjaHoraria y tvCreado, es buena práctica vincularlos
            binding.tvFranjaHoraria.text = solicitud.franjaHoraria // Asumiendo que muestra la franja
            binding.tvCreado.text = context.getString(R.string.created_at_format, solicitud.createdAt) // Asumiendo que existe created_at_format

            // 3. Limpiar y Ocultar TODOS los botones de acción para empezar de cero
            binding.btnCancelClient.visibility = View.GONE
            binding.btnAcceptDriver.visibility = View.GONE
            binding.btnStartRouteDriver.visibility = View.GONE
            binding.btnDeliverDriver.visibility = View.GONE
            binding.btnConfirmDelivery.visibility = View.GONE

            // 4. Lógica de Acciones según el Rol y Estado
            when (role) {
                "CLIENTE" -> handleClientActions(solicitud)
                "GESTOR" -> handleGestorActions(solicitud)
                "CONDUCTOR" -> handleConductorActions(solicitud)
            }

            // 5. Configurar Colores del Estado
            val estadoColorRes = when (solicitud.estado.uppercase()) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA" -> R.color.status_assigned
                "INICIADA" -> R.color.status_in_progress
                "ENTREGADA", "FINALIZADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_error
                else -> R.color.on_surface_secondary
            }
            binding.tvEstado.setTextColor(ContextCompat.getColor(context, estadoColorRes))
        }

        private fun handleClientActions(solicitud: Solicitud) {
            val esActiva = solicitud.estado.uppercase() in listOf("PENDIENTE", "ASIGNADA")

            if (esActiva) {
                binding.btnCancelClient.visibility = View.VISIBLE
                binding.btnCancelClient.setOnClickListener {
                    onActionClick(solicitud, "CANCELAR_CLIENTE")
                }
            }
        }

        private fun handleGestorActions(solicitud: Solicitud) {
            // El gestor necesita un botón para ASIGNAR.
            // Usaremos btnAcceptDriver para esta acción si está PENDIENTE.
            val context = itemView.context

            if (solicitud.estado.uppercase() == "PENDIENTE") {
                binding.btnAcceptDriver.apply { // Reutilizamos el botón del conductor como botón principal
                    visibility = View.VISIBLE
                    text = context.getString(R.string.action_assign) // Usar 'action_assign'
                    setOnClickListener {
                        this@SolicitudAdapter.onActionClick(solicitud, "ASIGNAR")
                    }
                }
            }
        }

        private fun handleConductorActions(solicitud: Solicitud) {
            val context = itemView.context

            when (solicitud.estado.uppercase()) {
                "ASIGNADA" -> {
                    // Botón principal: Aceptar / Iniciar recolección
                    binding.btnStartRouteDriver.apply { // Usaremos StartRoute para iniciar la recolección
                        visibility = View.VISIBLE
                        // Usamos un String más adecuado para INICIAR el proceso de recolección
                        text = context.getString(R.string.action_start)
                        setOnClickListener {
                            this@SolicitudAdapter.onActionClick(solicitud, "INICIAR")
                        }
                    }
                    // Botón secundario: Cancelar (opcional)
                    binding.btnCancelClient.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.action_cancel)
                        setOnClickListener {
                            this@SolicitudAdapter.onActionClick(solicitud, "CANCELAR_CONDUCTOR")
                        }
                    }
                }
                "INICIADA" -> {
                    // Botón principal: Finalizar recolección/Entrega (depende del flujo)
                    binding.btnDeliverDriver.apply { // Usaremos DeliverDriver para finalizar
                        visibility = View.VISIBLE
                        text = context.getString(R.string.action_finish) // Usar 'action_finish'
                        setOnClickListener {
                            this@SolicitudAdapter.onActionClick(solicitud, "FINALIZAR")
                        }
                    }
                    // Botón secundario: Cancelar (opcional)
                    binding.btnCancelClient.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.action_cancel)
                        setOnClickListener {
                            this@SolicitudAdapter.onActionClick(solicitud, "CANCELAR_CONDUCTOR")
                        }
                    }
                }
                // Nota: Los estados "ENTREGADA" o "FINALIZADA" no suelen tener botones de acción.
            }
        }
    }

    // --- Métodos del Adapter ---

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val binding = ItemSolicitudBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SolicitudViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }
}