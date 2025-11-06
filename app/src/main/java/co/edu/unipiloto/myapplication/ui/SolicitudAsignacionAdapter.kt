// Archivo: co.edu.unipiloto.myapplication.ui/SolicitudAsignacionAdapter.kt (COMPLETO)

package co.edu.unipiloto.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.db.SolicitudRepository

/**
 * Adaptador para mostrar solicitudes pendientes de asignación de conductor.
 * Muestra el layout item_solicitud_pendiente.xml
 */
class SolicitudAsignacionAdapter(
    private var items: List<SolicitudRepository.Solicitud>,
    // Datos del conductor
    private val conductores: List<Pair<Long, String>>, // Pair<ID_Conductor, Nombre>
    // Dependencia del repositorio para ejecutar la acción de asignación
    private val solicitudRepository: SolicitudRepository,
    // Callback para recargar la lista en el Fragmento/Activity
    private val onAssignmentSuccess: () -> Unit
) : RecyclerView.Adapter<SolicitudAsignacionAdapter.ViewHolder>() {

    /**
     * Replaces the adapter's items with the provided list and refreshes the displayed list.
     *
     * @param newItems The new list of solicitudes to display.
     */
    fun updateData(newItems: List<SolicitudRepository.Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Inflates the item_solicitud_pendiente layout and returns a ViewHolder for the new item view.
     *
     * @param parent The parent view group used to inflate layout parameters.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder backed by the inflated item view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud_pendiente, parent, false)
        return ViewHolder(view)
    }

    /**
     * Bind a Solicitud item to the holder and configure the driver selection and assignment actions.
     *
     * Sets visible fields (request ID, destination summary, and sender ID), populates the conductor
     * spinner with the provided driver names (preceded by a "select driver" prompt), and attaches
     * a click listener to the assign button that validates selection, calls the repository to assign
     * the selected driver to the solicitud, displays a success/failure toast, and invokes the
     * onAssignmentSuccess callback when the assignment succeeds.
     *
     * @param holder ViewHolder containing views to bind and configure.
     * @param position Adapter position of the Solicitud item to bind.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 1. Asignación de datos a las vistas
        holder.tvSolicitudID.text = context.getString(R.string.guide_example, item.id.toString())

        // El objeto Solicitud tiene direcciónId y zona, pero no la dirección completa.
        // Asignamos la dirección para mantener el contexto visual.
        holder.tvDestination.text = "Dir ID: ${item.direccionId} (Zona: ${item.zona})"
        holder.tvSender.text = "Cliente ID: ${item.userId}" // Mostrar ID temporalmente


        // 2. Configuración del Spinner de Conductores
        val conductorNames = conductores.map { it.second }.toMutableList()
        conductorNames.add(0, context.getString(R.string.select_driver_prompt))

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, conductorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spConductor.adapter = adapter

        // 3. Listener del botón de Asignar
        holder.btnAssign.setOnClickListener {
            if (holder.spConductor.selectedItemPosition <= 0) {
                Toast.makeText(context, "Debe seleccionar un conductor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener el ID del conductor seleccionado
            val selectedIndex = holder.spConductor.selectedItemPosition - 1
            val selectedDriverId = conductores[selectedIndex].first
            val solicitudId = item.id

            // Ejecutar la asignación usando el repositorio inyectado
            val rowsAffected = solicitudRepository.asignarRecolector(solicitudId, selectedDriverId)

            if (rowsAffected > 0) {
                Toast.makeText(context, "Guía $solicitudId asignada exitosamente.", Toast.LENGTH_SHORT).show()
                onAssignmentSuccess.invoke() // Llama al callback para recargar la lista en el fragmento
            } else {
                Toast.makeText(context, "Fallo al asignar. La solicitud ya no está PENDIENTE.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
 * Provides the number of solicitudes currently held by the adapter.
 *
 * @return The count of items displayed by the adapter.
 */
override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Mapeo de vistas de item_solicitud_pendiente.xml
        val tvSolicitudID: TextView = itemView.findViewById(R.id.tvSolicitudID)
        val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        val spConductor: Spinner = itemView.findViewById(R.id.spConductor)
        val btnAssign: Button = itemView.findViewById(R.id.btnAssign)
    }
}