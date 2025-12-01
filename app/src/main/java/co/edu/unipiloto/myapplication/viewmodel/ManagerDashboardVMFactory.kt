// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/ManagerDashboardVMFactory.kt

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.UserRepository // 🚨 NECESARIO

/**
 * Factory para crear instancias de ManagerDashboardViewModel con las dependencias requeridas.
 */
class ManagerDashboardVMFactory(
    private val solicitudRepository: SolicitudRepository,
    // 🏆 CORRECCIÓN: Añadir UserRepository
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 🏆 CORRECCIÓN: Pasar AMBOS repositorios al ViewModel
            return ManagerDashboardViewModel(solicitudRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}