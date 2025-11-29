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
 * Adaptador para mostrar la lista de Sucursales en la RecyclerView.
 * Utiliza ListAdapter con DiffUtil para manejar eficientemente las actualizaciones de la lista.
 *
 * @param onEditClick Callback para manejar la acción de editar una sucursal.
 * @param onDeleteClick Callback para manejar la acción de eliminar una sucursal.
 */
class SucursalAdapter(
    private val onEditClick: (SucursalResponse) -> Unit,
    private val onDeleteClick: (SucursalResponse) -> Unit
) : ListAdapter<SucursalResponse, SucursalAdapter.SucursalViewHolder>(SucursalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SucursalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)
        return SucursalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SucursalViewHolder, position: Int) {
        val sucursal = getItem(position)
        holder.bind(sucursal)
    }

    inner class SucursalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBranchName: TextView = itemView.findViewById(R.id.tvBranchName)
        private val tvBranchAddress: TextView = itemView.findViewById(R.id.tvBranchAddress)
        private val tvBranchId: TextView = itemView.findViewById(R.id.tvBranchId)
        private val btnEditBranch: ImageButton = itemView.findViewById(R.id.btnEditBranch)
        private val btnDeleteBranch: ImageButton = itemView.findViewById(R.id.btnDeleteBranch)

        fun bind(sucursal: SucursalResponse) {
            tvBranchName.text = sucursal.nombre
            tvBranchAddress.text = "${sucursal.direccion.direccionCompleta}, ${sucursal.direccion.ciudad}"
            tvBranchId.text = itemView.context.getString(R.string.branch_id_format, sucursal.id.toString())

            // Configuración de los botones de acción
            btnEditBranch.setOnClickListener { onEditClick(sucursal) }
            btnDeleteBranch.setOnClickListener { onDeleteClick(sucursal) }
        }
    }

    /**
     * Callback para DiffUtil. Compara elementos viejos y nuevos de la lista para optimizar las actualizaciones de la RecyclerView.
     */
    class SucursalDiffCallback : DiffUtil.ItemCallback<SucursalResponse>() {
        override fun areItemsTheSame(oldItem: SucursalResponse, newItem: SucursalResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SucursalResponse, newItem: SucursalResponse): Boolean {
            return oldItem == newItem
        }
    }
}