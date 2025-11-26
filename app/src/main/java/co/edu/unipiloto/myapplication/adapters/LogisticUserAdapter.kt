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
import co.edu.unipiloto.myapplication.model.LogisticUser // Asegúrate de que este modelo exista

/**
 * Adaptador para mostrar la lista de usuarios logísticos en el RecyclerView.
 *
 * @param onEditClick Callback que se ejecuta al presionar el botón de editar.
 * @param onDeleteClick Callback que se ejecuta al presionar el botón de eliminar.
 */
class LogisticUserAdapter(
    private val onEditClick: (LogisticUser) -> Unit,
    private val onDeleteClick: (LogisticUser) -> Unit
) : ListAdapter<LogisticUser, LogisticUserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logistic_user, parent, false) // Referencia al layout del ítem
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, onEditClick, onDeleteClick)
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvUserRoleZone: TextView = itemView.findViewById(R.id.tvUserRoleZone)
        private val tvIsActive: TextView = itemView.findViewById(R.id.tvIsActive)
        private val ivUserIcon: ImageView = itemView.findViewById(R.id.ivUserIcon)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(user: LogisticUser, onEditClick: (LogisticUser) -> Unit, onDeleteClick: (LogisticUser) -> Unit) {

            tvUserName.text = user.fullName

            // Construir texto de Rol y Zona (Sucursal)
            val roleZoneText = if (user.sucursal?.nombre.isNullOrEmpty()) {
                user.role
            } else {
                "${user.role} - ${user.sucursal?.nombre}"
            }
            tvUserRoleZone.text = roleZoneText

            // Manejar el estado ACTIVO/INACTIVO y su estilo (background drawable)
            if (user.isActive) {
                tvIsActive.text = "ACTIVO"
                // Referencia al drawable que creaste
                tvIsActive.setBackgroundResource(R.drawable.bg_status_active)
            } else {
                tvIsActive.text = "INACTIVO"
                // Referencia al drawable que creaste
                tvIsActive.setBackgroundResource(R.drawable.bg_status_inactive)
            }

            // Asignar ícono basado en el rol (Puedes expandir esto)
            val iconRes = when (user.role) {
                "CONDUCTOR" -> R.drawable.ic_truck_driver // Asumo que tienes un ícono para conductor
                "FUNCIONARIO", "GESTOR", "ANALISTA" -> R.drawable.ic_person
                else -> R.drawable.ic_person_outline // Ícono de respaldo
            }
            ivUserIcon.setImageResource(iconRes)

            // Listeners para las acciones de CRUD
            btnEdit.setOnClickListener { onEditClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    /**
     * Define cómo el ListAdapter debe comparar los ítems para animar los cambios.
     */
    private class UserDiffCallback : DiffUtil.ItemCallback<LogisticUser>() {
        override fun areItemsTheSame(oldItem: LogisticUser, newItem: LogisticUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LogisticUser, newItem: LogisticUser): Boolean {
            return oldItem == newItem
        }
    }

}