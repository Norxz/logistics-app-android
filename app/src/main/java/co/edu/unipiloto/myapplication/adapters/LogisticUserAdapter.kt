package co.edu.unipiloto.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.model.User // Importar el modelo de datos
import androidx.core.content.ContextCompat // Necesario para la lógica de recursos

/**
 * Adaptador para mostrar la lista de usuarios logísticos en el RecyclerView.
 * Hereda de ListAdapter para manejar automáticamente las actualizaciones de lista con DiffUtil.
 *
 * @param onEditClick Callback que se ejecuta al presionar el botón de editar.
 * @param onDeleteClick Callback que se ejecuta al presionar el botón de eliminar.
 */
class LogisticUserAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, LogisticUserAdapter.UserViewHolder>(UserDiffCallback()) {

    // --- 1. ViewHolder ---

    /**
     * Clase interna que contiene y gestiona las vistas de cada elemento de la lista.
     */
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // IDs corregidos para que coincidan con el layout XML (item_logistic_user.xml)
        private val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvRoleZone: TextView = itemView.findViewById(R.id.tvUserRoleZone)
        private val tvIsActive: TextView = itemView.findViewById(R.id.tvIsActive)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(user: User) {
            tvName.text = user.fullName

            // Lógica para mostrar Rol y Sucursal
            val roleSucursal = if (user.sucursal == null || user.sucursal.nombre.isNullOrEmpty()) {
                user.role
            } else {
                "${user.role} | Sucursal: ${user.sucursal.nombre}"
            }
            tvRoleZone.text = roleSucursal

            // Lógica para el indicador de estado (usando el TextView tvIsActive)
            val context = itemView.context
            if (user.isActive) {
                tvIsActive.text = "ACTIVO"
                tvIsActive.background = ContextCompat.getDrawable(context, R.drawable.bg_status_active)
            } else {
                tvIsActive.text = "INACTIVO"
                // 🚀 CORRECCIÓN DEL TYPO: Se cambia 'ContextPat' a 'ContextCompat'
                tvIsActive.background = ContextCompat.getDrawable(context, R.drawable.bg_status_inactive)
            }

            // Listeners para los botones
            btnEdit.setOnClickListener {
                onEditClick(user)
            }
            btnDelete.setOnClickListener {
                onDeleteClick(user)
            }
        }
    }

    // --- 2. Implementación de ListAdapter ---

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logistic_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

// --- 3. DiffUtil.ItemCallback ---

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}