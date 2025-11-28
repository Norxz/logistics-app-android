package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import java.lang.IllegalArgumentException

/**
 * 🏭 Fábrica de ViewModel para SolicitudViewModel.
 * Permite la inyección de la dependencia SolicitudRepository en el constructor del ViewModel.
 */
class SolicitudViewModelFactory(
    private val repository: SolicitudRepository // ⬅️ Dependencia que se debe inyectar
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica si la clase solicitada es SolicitudViewModel
        if (modelClass.isAssignableFrom(SolicitudViewModel::class.java)) {
            // Si lo es, crea una instancia pasándole el repositorio
            return SolicitudViewModel(repository) as T
        }
        // Si no es la clase esperada, lanza una excepción
        throw IllegalArgumentException("Unknown ViewModel class requested")
    }
}