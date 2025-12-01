package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.databinding.ItemSolicitudBinding
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.User
import android.widget.Toast

/**
 * Define el tipo para el listener de clic en las acciones.
 * Recibe el modelo de Solicitud, la acción (String) realizada y el ID del gestor (Long?).
 */
typealias OnSolicitudActionListener = (Solicitud, String, Long?) -> Unit

class SolicitudAdapter(
    private var items: List<Solicitud>,
    private val role: String,
    private val onActionClick: OnSolicitudActionListener
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    private var usersForAssignment: List<User> = emptyList()
    private val selectedGestorIdMap = mutableMapOf<Long, Long>()

    // --- ViewHolder ---

    inner class SolicitudViewHolder(private val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun setupGestorSelector(solicitudId: Long) {
            val context = itemView.context

            if (usersForAssignment.isEmpty()) return

            val userNames = usersForAssignment.map { it.fullName }.toMutableList()
            // Título del Spinner
            userNames.add(0, context.getString(R.string.placeholder_select_gestor))

            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                userNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            binding.spinnerGestorAsignar.adapter = adapter

            binding.spinnerGestorAsignar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        val gestor = usersForAssignment.getOrNull(position - 1)
                        if (gestor?.id != null) {
                            selectedGestorIdMap[solicitudId] = gestor.id
                        }
                    } else {
                        selectedGestorIdMap.remove(solicitudId)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedGestorIdMap.remove(solicitudId)
                }
            }

            val previouslySelectedId = selectedGestorIdMap[solicitudId]
            val index = usersForAssignment.indexOfFirst { it.id == previouslySelectedId }

            val selectionIndex = if (index >= 0) index + 1 else 0
            binding.spinnerGestorAsignar.setSelection(selectionIndex)

            if (selectionIndex > 0) {
                val initialGestorId = usersForAssignment.get(selectionIndex - 1).id
                if (initialGestorId != null) {
                    selectedGestorIdMap[solicitudId] = initialGestorId
                }
            }
        }

        fun bind(solicitud: Solicitud) {
            val context = itemView.context
            val solicitudId = solicitud.id ?: return

            // 1. Mostrar Datos Principales
            binding.tvSolicitudId.text = context.getString(R.string.solicitud_id_format, solicitudId)
            binding.tvDireccion.text = solicitud.direccionEntrega.direccionCompleta
            binding.tvEstado.text = solicitud.estado

            // 2. Mostrar Conductor/Sucursal (Información Adicional)
            // Se asume que 'solicitud.conductor' contiene el responsable del servicio (sea Gestor o Conductor)
            val infoExtra = when {
                solicitud.conductor != null -> context.getString(R.string.recolector_assigned_format, solicitud.conductor.fullName)
                solicitud.sucursal != null -> context.getString(R.string.branch_origin_format, solicitud.sucursal.nombre)
                else -> ""
            }
            binding.tvInfoExtra.text = infoExtra
            binding.tvInfoExtra.visibility = if (infoExtra.isNotEmpty()) View.VISIBLE else View.GONE

            binding.tvFranjaHoraria.text = solicitud.franjaHoraria
            binding.tvCreado.text = context.getString(R.string.created_at_format, solicitud.createdAt)

            // 3. Limpiar y Ocultar TODOS los elementos de acción al inicio
            binding.btnCancelClient.visibility = View.GONE
            binding.btnAcceptDriver.visibility = View.GONE
            binding.btnStartRouteDriver.visibility = View.GONE
            binding.btnConfirmDelivery.visibility = View.GONE
            binding.spinnerGestorAsignar.visibility = View.GONE
            binding.btnAsignarGestor.visibility = View.GONE
            binding.llActions.visibility = View.GONE
            binding.tilConfirmCode.visibility = View.GONE
            binding.etConfirmCode.text = null


            // 4. Lógica de Acciones según el Rol y Estado
            val estadoUpper = solicitud.estado.uppercase()

            when (role) {
                // Lógica de Asignación a Gestor/Funcionario
                "FUNCIONARIO", "GESTOR" -> {
                    if (estadoUpper == "PENDIENTE") {
                        handleAssignmentRoleActions(solicitud)
                    }
                }
                // Lógica de Acciones del Cliente
                "CLIENTE" -> handleClientActions(solicitud)
                // Lógica de Acciones del Conductor (Driver)
                "CONDUCTOR" -> handleConductorActions(solicitud)
            }

            // 5. Asignación de Color al Estado
            val estadoColorRes = when (estadoUpper) {
                "PENDIENTE" -> R.color.status_pending
                "ASIGNADA" -> R.color.status_assigned
                "EN_RUTA_RECOLECCION", "EN_DISTRIBUCION", "EN_RUTA_REPARTO" -> R.color.status_in_progress
                "ENTREGADA" -> R.color.status_success
                "CANCELADA" -> R.color.status_error
                else -> R.color.on_surface_secondary
            }
            binding.tvEstado.setTextColor(ContextCompat.getColor(context, estadoColorRes))
        }

        /**
         * Lógica para mostrar la interfaz de Asignación.
         * Usado por los roles FUNCIONARIO y GESTOR.
         */
        private fun handleAssignmentRoleActions(solicitud: Solicitud) {
            val context = itemView.context
            val idSolicitud = solicitud.id ?: return

            if (usersForAssignment.isNotEmpty()) {

                binding.spinnerGestorAsignar.visibility = View.VISIBLE
                binding.btnAsignarGestor.visibility = View.VISIBLE

                setupGestorSelector(idSolicitud)

                binding.btnAsignarGestor.setOnClickListener {
                    val gestorId = selectedGestorIdMap[idSolicitud]

                    if (gestorId != null) {
                        this@SolicitudAdapter.onActionClick(solicitud, "ASIGNAR", gestorId)
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_select_gestor), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        private fun handleClientActions(solicitud: Solicitud) {
            val esActiva = solicitud.estado.uppercase() in listOf("PENDIENTE", "ASIGNADA")

            if (esActiva) {
                binding.llActions.visibility = View.VISIBLE
                binding.btnCancelClient.visibility = View.VISIBLE
                binding.btnCancelClient.setOnClickListener {
                    onActionClick(solicitud, "CANCELAR_CLIENTE", null)
                }
            }
        }

        /**
         * Lógica para mostrar las acciones del Conductor (Driver).
         * Usado solo por el rol CONDUCTOR.
         */
        private fun handleConductorActions(solicitud: Solicitud) {
            val context = itemView.context
            val estadoUpper = solicitud.estado.uppercase()

            binding.llActions.visibility = View.VISIBLE

            // Se utiliza R.color.md_white asumiendo que es el ID de recurso para el color blanco
            val colorWhite = ContextCompat.getColorStateList(context, R.color.md_white)
            val colorPrimary = ContextCompat.getColor(context, R.color.primary_color)
            val colorSuccess = ContextCompat.getColorStateList(context, R.color.status_success)


            when (estadoUpper) {
                "ASIGNADA" -> {
                    // El conductor ACEPTA la solicitud
                    binding.btnAcceptDriver.visibility = View.VISIBLE
                    binding.btnAcceptDriver.setOnClickListener {
                        onActionClick(solicitud, "ACEPTAR_ASIGNACION", null)
                    }
                    // Restaurar el botón de ruta a su estilo Outlined Button original, por si fue modificado antes
                    binding.btnStartRouteDriver.backgroundTintList = colorWhite
                    binding.btnStartRouteDriver.setTextColor(colorPrimary)
                }
                "EN_RECOLECCION" -> {
                    // Acción: El conductor INICIA la ruta para recoger el paquete
                    binding.btnStartRouteDriver.visibility = View.VISIBLE
                    binding.btnStartRouteDriver.text = context.getString(R.string.action_start_recollection)
                    binding.btnStartRouteDriver.backgroundTintList = colorWhite
                    binding.btnStartRouteDriver.setTextColor(colorPrimary)
                    binding.btnStartRouteDriver.setOnClickListener {
                        onActionClick(solicitud, "INICIAR_RECOLECCION", null)
                    }
                }
                "EN_RUTA_RECOLECCION" -> {
                    // Acción: El conductor MARCA el paquete como RECOLECTADO
                    // REUTILIZAMOS btnStartRouteDriver
                    binding.btnStartRouteDriver.visibility = View.VISIBLE
                    binding.btnStartRouteDriver.text = context.getString(R.string.action_collect)

                    // Cambiamos el estilo a Button (Filled)
                    binding.btnStartRouteDriver.backgroundTintList = colorSuccess
                    binding.btnStartRouteDriver.setTextColor(ContextCompat.getColor(context, R.color.md_white)) // Usar R.color.white si md_white falla

                    binding.btnStartRouteDriver.setOnClickListener {
                        onActionClick(solicitud, "PAQUETE_RECOLECTADO", null)
                    }
                }
                "EN_RUTA_REPARTO" -> {
                    // Etapa final: Se requiere el código de confirmación
                    binding.llActions.visibility = View.GONE

                    binding.tilConfirmCode.visibility = View.VISIBLE
                    binding.btnConfirmDelivery.visibility = View.VISIBLE

                    binding.btnConfirmDelivery.setOnClickListener {
                        val code = binding.etConfirmCode.text.toString().trim()

                        if (code.length >= 4) {
                            val actionWithCode = "CONFIRMAR_ENTREGA:$code"
                            this@SolicitudAdapter.onActionClick(solicitud, actionWithCode, null)
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_confirmation_code_required), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else -> {
                    binding.llActions.visibility = View.GONE
                }
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
        val solicitud = items[position]
        holder.bind(solicitud)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateUsersForAssignment(newUsers: List<User>) {
        this.usersForAssignment = newUsers
        selectedGestorIdMap.clear()
        notifyDataSetChanged()
    }
}