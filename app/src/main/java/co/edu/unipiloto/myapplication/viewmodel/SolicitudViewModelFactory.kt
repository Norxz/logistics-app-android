package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.SucursalRepository

// Archivo: SolicitudViewModelFactory.kt
class SolicitudViewModelFactory(private val repository: SolicitudRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // ➡️ Instancia el ViewModel simple (solo SolicitudRepository)
            return SolicitudViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}