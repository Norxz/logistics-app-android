// Archivo: co.edu.unipiloto.myapplication.ui/AddBranchViewModelFactory.kt
package co.edu.unipiloto.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unipiloto.myapplication.repository.SucursalRepository

class AddBranchViewModelFactory(private val repository: SucursalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBranchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBranchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}