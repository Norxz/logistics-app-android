package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.dto.SucursalRequest
import co.edu.unipiloto.myapplication.repository.SucursalRepository
import kotlinx.coroutines.launch

// ⚠️ NECESITAS DEFINIR EL REPOSITORIO Y EL RETROFIT CLIENT EN TU PROYECTO
class AddBranchViewModel(private val repository: SucursalRepository) : ViewModel() {

    // LiveData para observar el resultado de la operación (éxito o error)
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult

    // [TODO] Añadir LiveData para cargar datos de edición si es necesario:
    // private val _branchData = MutableLiveData<SucursalResponse>()
    // val branchData: LiveData<SucursalResponse> = _branchData


    fun saveBranch(request: SucursalRequest) {
        viewModelScope.launch {
            try {
                // Suponiendo que el método saveBranch del repositorio es suspendido y maneja la API
                repository.saveBranch(request)
                _saveResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e))
            }
        }
    }

    fun updateBranch(id: Int, request: SucursalRequest) {
        viewModelScope.launch {
            try {
                // Suponiendo que el método updateBranch del repositorio es suspendido y maneja la API
                repository.updateBranch(id, request)
                _saveResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e))
            }
        }
    }

    // [TODO] Implementar loadBranchById si necesitas cargar datos en modo edición
}