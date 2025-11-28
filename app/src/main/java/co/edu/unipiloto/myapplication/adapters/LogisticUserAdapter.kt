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

/**
 * Adaptador para mostrar la lista de usuarios logísticos en el RecyclerView.
 *
 * @param onEditClick Callback que se ejecuta al presionar el botón de editar.
 * @param onDeleteClick Callback que se ejecuta al presionar el botón de eliminar.
 */
class UserAdapter(
    private val onEditClick: (Long) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) {



}