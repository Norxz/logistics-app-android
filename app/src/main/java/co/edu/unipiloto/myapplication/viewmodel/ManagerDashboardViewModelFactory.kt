// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/ManagerDashboardViewModelFactory.kt

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SolicitudRepository

/**
 * Factory para crear instancias de ManagerDashboardViewModel con las dependencias requeridas.
 */
class ManagerDashboardViewModelFactory(
    private val solicitudRepository: SolicitudRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManagerDashboardViewModel(solicitudRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}