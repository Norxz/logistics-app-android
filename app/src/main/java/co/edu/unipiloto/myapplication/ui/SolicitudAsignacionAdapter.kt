// Archivo: co.edu.unipiloto.myapplication.ui/SolicitudAsignacionAdapter.kt (COMPLETO Y CORREGIDO)

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
// 🏆 CORRECCIÓN CLAVE: Importar la clase de modelo Solicitud
import co.edu.unipiloto.myapplication.models.Solicitud

/**
 * Adaptador para mostrar solicitudes pendientes de asignación de conductor.
 * Muestra el layout item_solicitud_pendiente.xml
 */
class SolicitudAsignacionAdapter(
    // 🏆 CORRECCIÓN: Usar la clase de modelo Solicitud importada
    private var items: List<Solicitud>,
    // Datos del conductor
    private val conductores: List<Pair<Long, String>>, // Pair<ID_Conductor, Nombre>
    // Dependencia del repositorio para ejecutar la acción de asignación
    private val solicitudRepository: SolicitudRepository,
    // Callback para recargar la lista en el Fragmento/Activity
    private val onAssignmentSuccess: () -> Unit
) : RecyclerView.Adapter<SolicitudAsignacionAdapter.ViewHolder>() {

    // -----------------------------------------------------
    // MÉTODOS PÚBLICOS
    // -----------------------------------------------------

    // 🏆 CORRECCIÓN: El parámetro newItems debe usar la clase Solicitud importada
    fun updateData(newItems: List<Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }

    // -----------------------------------------------------
    // MÉTODOS DEL RECYCLERVIEW
    // -----------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud_pendiente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 1. Asignación de datos a las vistas
        holder.tvSolicitudID.text = context.getString(R.string.guide_example, item.id.toString())

        // El objeto Solicitud enriquecido SÍ tiene fullAddress y clientName
        holder.tvDestination.text = item.fullAddress // Usar la dirección completa
        holder.tvSender.text = "Cliente: ${item.clientName}" // Usar el nombre del cliente


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
                // Esto podría fallar si la solicitud ya no es PENDIENTE
                Toast.makeText(context, "Fallo al asignar. La solicitud ya no está PENDIENTE o hubo un error.", Toast.LENGTH_LONG).show()
            }
        }
    }

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