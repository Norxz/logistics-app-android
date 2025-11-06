package co.edu.unipiloto.myapplication.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.models.User // Asegúrate que esta importación sea correcta

/**
 * Adaptador para mostrar la lista de usuarios logísticos en el RecyclerView.
 */
class UserAdapter(
    private var userList: MutableList<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        // ID CAMBIADO
        val tvUserRoleZone: TextView = itemView.findViewById(R.id.tvUserRoleZone)
        // ID NUEVO
        val tvIsActive: TextView = itemView.findViewById(R.id.tvIsActive)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(user: User) {
            tvUserName.text = user.fullName

            // Mostrar Rol y Sucursal (usando el nuevo ID tvUserRoleZone)
            val roleSucursal = if (user.sucursal.isNullOrBlank()) {
                user.role
            } else {
                "${user.role} | Zona: ${user.sucursal}"
            }
            tvUserRoleZone.text = roleSucursal

            // Lógica para el indicador de estado (tvIsActive)
            val context = itemView.context
            if (user.isActive) {
                tvIsActive.text = "ACTIVO"
                tvIsActive.background = ContextCompat.getDrawable(context, R.drawable.bg_status_active)
            } else {
                tvIsActive.text = "INACTIVO"
                tvIsActive.background = ContextCompat.getDrawable(context, R.drawable.bg_status_inactive)
                // Asumo que tienes un drawable bg_status_inactive
            }

            // Manejo de eventos de clic
            btnEdit.setOnClickListener { onEditClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logistic_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}