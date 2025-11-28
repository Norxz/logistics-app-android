// Archivo: SolicitudAdapter.kt
package co.edu.unipiloto.myapplication.ui.adapter // <--- Ajusta este paquete si es necesario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R // Asegúrate de que este R sea correcto
import co.edu.unipiloto.myapplication.databinding.ItemSolicitudBinding // ⬅️ ASUMIENDO ESTE NOMBRE DE BINDING
import co.edu.unipiloto.myapplication.model.Solicitud // ⬅️ IMPORTAR EL MODELO

/**
 * Define el tipo para el listener de clic en las acciones (CANCELAR, CONFIRMAR).
 * Recibe el modelo de Solicitud y la acción (String) realizada.
 */
typealias OnSolicitudActionListener = (Solicitud, String) -> Unit

class SolicitudAdapter(
    private var items: List<Solicitud>,
    private val role: String,

    // 2. El listener que usa la Activity
    private val onActionClick: OnSolicitudActionListener
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    // --- ViewHolder ---

    inner class SolicitudViewHolder(private val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: Solicitud) {
            // 🚨 Aquí va la lógica para mostrar los datos de la Solicitud
            binding.tvSolicitudId.text = itemView.context.getString(R.string.solicitud_id_format, solicitud.id ?: 0L)
            binding.tvDireccion.text = solicitud.direccion.direccionCompleta
            binding.tvEstado.text = solicitud.estado


            // Ejemplo de botón de acción:
            val esActiva = solicitud.estado.uppercase() !in listOf("ENTREGADA", "FINALIZADA", "CANCELADA")

            if (role == "CLIENTE" && esActiva) {
                binding.btnCancelClient.visibility = View.VISIBLE
                binding.btnCancelClient.setOnClickListener {
                    onActionClick(solicitud, "CANCELAR_CLIENTE")
                }
            } else {
                binding.btnCancelClient.visibility = View.GONE
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

    // --- Método expuesto al ViewModel para actualizar la lista ---

    /**
     * Actualiza la lista de solicitudes y notifica al RecyclerView para que se redibuje.
     */
    fun updateData(newItems: List<Solicitud>) {
        items = newItems
        notifyDataSetChanged()
    }
}