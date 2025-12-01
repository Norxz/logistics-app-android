// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/NewRequestCreationVMFactory.kt

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.SucursalRepository

/**
 * Factory para crear instancias de NewRequestCreationViewModel con dos dependencias.
 * 🎯 Resuelve los errores 'NewRequestCreationVMFactory'.
 */
class NewRequestCreationVMFactory(
    private val solicitudRepository: SolicitudRepository,
    private val sucursalRepository: SucursalRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewRequestCreationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewRequestCreationViewModel(solicitudRepository, sucursalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}