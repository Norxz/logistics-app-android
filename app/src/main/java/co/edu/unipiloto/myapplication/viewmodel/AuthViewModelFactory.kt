package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.AuthRepository
import java.lang.IllegalArgumentException

/**
 * 🏭 Fábrica de ViewModel para AuthViewModel.
 * Implementa ViewModelProvider.Factory para permitir la inyección del AuthRepository
 * en el constructor del AuthViewModel.
 */
class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Si la clase es AuthViewModel, la creamos pasándole el repository
            return AuthViewModel(repository) as T
        }
        // Si intenta crear otra clase de ViewModel, lanzamos una excepción
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}