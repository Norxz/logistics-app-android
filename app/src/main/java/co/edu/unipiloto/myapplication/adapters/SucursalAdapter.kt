package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.SucursalResponse

/**
 * Adaptador para mostrar la lista de Sucursales en la RecyclerView usando ListAdapter.
 * * Acepta dos callbacks: uno para la edición y otro para la eliminación.
 *
 * @param onEditClick Callback para manejar la acción de editar una sucursal.
 * @param onDeleteClick Callback para manejar la acción de eliminar una sucursal.
 */
class SucursalAdapter(
    // ✅ FIRMA CORREGIDA: Acepta explícitamente ambos callbacks
    private val onEditClick: (SucursalResponse) -> Unit,
    private val onDeleteClick: (SucursalResponse) -> Unit
) : ListAdapter<SucursalResponse, SucursalAdapter.SucursalViewHolder>(SucursalDiffCallback()) {

    /**
     * ✅ Método de conveniencia para resolver el error 'updateData' en la Activity.
     * Llama internamente a submitList(), el método correcto de ListAdapter.
     */
    fun updateData(newBranches: List<SucursalResponse>) {
        submitList(newBranches)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SucursalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)
        return SucursalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SucursalViewHolder, position: Int) {
        val sucursal = getItem(position)
        holder.bind(sucursal)
    }

    // --- ViewHolder ---
    inner class SucursalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBranchName: TextView = itemView.findViewById(R.id.tvBranchName)
        private val tvBranchAddress: TextView = itemView.findViewById(R.id.tvBranchAddress)
        private val tvBranchId: TextView = itemView.findViewById(R.id.tvBranchId)
        private val btnEditBranch: ImageButton = itemView.findViewById(R.id.btnEditBranch)
        private val btnDeleteBranch: ImageButton = itemView.findViewById(R.id.btnDeleteBranch)

        fun bind(sucursal: SucursalResponse) {
            tvBranchName.text = sucursal.nombre
            // Asumiendo que Direccion es un objeto con direccionCompleta y ciudad
            tvBranchAddress.text = "${sucursal.direccion.direccionCompleta}, ${sucursal.direccion.ciudad}"
            // Asumiendo que tienes un recurso de cadena llamado branch_id_format
            tvBranchId.text = itemView.context.getString(R.string.branch_id_format, sucursal.id.toString())

            // ✅ Configuración de los botones de acción usando los callbacks del constructor
            btnEditBranch.setOnClickListener { onEditClick(sucursal) }
            btnDeleteBranch.setOnClickListener { onDeleteClick(sucursal) }
        }
    }

    // --- DiffUtil Callback ---
    /**
     * Callback para DiffUtil. Compara elementos viejos y nuevos de la lista para optimizar las actualizaciones de la RecyclerView.
     */
    class SucursalDiffCallback : DiffUtil.ItemCallback<SucursalResponse>() {
        override fun areItemsTheSame(oldItem: SucursalResponse, newItem: SucursalResponse): Boolean {
            // Compara IDs para ver si representan la misma sucursal
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SucursalResponse, newItem: SucursalResponse): Boolean {
            // Compara todo el objeto para ver si los datos han cambiado
            return oldItem == newItem
        }
    }
}