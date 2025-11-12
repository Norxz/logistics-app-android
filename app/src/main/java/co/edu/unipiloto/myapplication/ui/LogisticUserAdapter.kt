package co.edu.unipiloto.myapplication.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.models.LogisticUser

class LogisticUserAdapter(
    private val userList: List<LogisticUser>,
    // Callback para manejar las acciones (Editar o Eliminar)
    private val onItemActionClicked: (LogisticUser, ActionType) -> Unit
) : RecyclerView.Adapter<LogisticUserAdapter.LogisticUserViewHolder>() {

    // Define los tipos de acciones que se pueden realizar en el ítem
    enum class ActionType { EDIT, DELETE }

    inner class LogisticUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Vistas actualizadas según tu nuevo item_logistic_user.xml
        val ivUserIcon: ImageView = view.findViewById(R.id.ivUserIcon)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserRoleZone: TextView = view.findViewById(R.id.tvUserRoleZone)
        val tvIsActive: TextView = view.findViewById(R.id.tvIsActive)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit) // Nuevo ID
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete) // Nuevo ID

        fun bind(user: LogisticUser) {
            tvUserName.text = user.name

            val roleZoneText = "${user.role} | Zona: ${user.sucursal ?: "N/A"}"
            tvUserRoleZone.text = roleZoneText

            // 1. Manejo del estado Activo/Inactivo (Pastilla)
            if (user.isActive) {
                tvIsActive.text = "ACTIVO"
                // Asegúrate de crear este drawable si aún no existe
                tvIsActive.setBackgroundResource(R.drawable.bg_status_active)
            } else {
                tvIsActive.text = "INACTIVO"
                // Asegúrate de crear este drawable si aún no existe
                tvIsActive.setBackgroundResource(R.drawable.bg_status_inactive)
            }

            // 2. Manejo del icono según el rol (Opcional, pero mejora la UX)
            // Asegúrate de tener drawables como ic_driver o ic_official
            when (user.role.uppercase()) {
                "CONDUCTOR" -> ivUserIcon.setImageResource(R.drawable.ic_driver)
                "FUNCIONARIO", "GESTOR" -> ivUserIcon.setImageResource(R.drawable.ic_official)
                else -> ivUserIcon.setImageResource(R.drawable.ic_person)
            }


            // 3. Manejar clics de acción
            btnEdit.setOnClickListener {
                onItemActionClicked(user, ActionType.EDIT)
            }

            btnDelete.setOnClickListener {
                onItemActionClicked(user, ActionType.DELETE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogisticUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logistic_user, parent, false)
        return LogisticUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogisticUserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}