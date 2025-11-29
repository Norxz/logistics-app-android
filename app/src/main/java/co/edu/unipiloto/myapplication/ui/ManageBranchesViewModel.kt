package co.edu.unipiloto.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import co.edu.unipiloto.myapplication.repository.SucursalRepository // Necesitarás este Repositorio
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel para gestionar los datos y la lógica de negocio de ManageBranchesActivity.
 */
class ManageBranchesViewModel(private val repository: SucursalRepository) : ViewModel() {

    // Lista de sucursales que se observará en la Activity
    private val _branches = MutableLiveData<List<SucursalResponse>>()
    val branches: LiveData<List<SucursalResponse>> = _branches

    // Copia de todas las sucursales cargadas (para filtrado)
    private var allBranches: List<SucursalResponse> = emptyList()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Carga las sucursales desde el Repositorio.
     */
    fun loadBranches() {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.listarSucursales() // Usar listarSucursales() del Repositorio

            _loading.value = false
            result.onSuccess { list ->
                allBranches = list
                _branches.value = list
            }.onFailure { exception ->
                _errorMessage.value = "Error al cargar sucursales: ${exception.message}"
            }
        }
    }

    /**
     * Filtra la lista de sucursales basada en el texto de búsqueda.
     */
    fun filterBranches(query: String) {
        val filteredList = if (query.isEmpty()) {
            allBranches
        } else {
            val lowerCaseQuery = query.lowercase()
            allBranches.filter { sucursal ->
                sucursal.nombre.lowercase().contains(lowerCaseQuery) ||
                        sucursal.direccion.ciudad.lowercase().contains(lowerCaseQuery)
            }
        }
        _branches.value = filteredList
    }

    /**
     * Elimina una sucursal por su ID.
     */
    fun deleteBranch(sucursal: SucursalResponse) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.eliminarSucursal(sucursal.id!!) // Usar eliminarSucursal() del Repositorio

            _loading.value = false
            result.onSuccess {
                _errorMessage.value = "Sucursal ${sucursal.nombre} eliminada con éxito."
                loadBranches() // Recargar la lista después de la eliminación
            }.onFailure { exception ->
                _errorMessage.value = "Error al eliminar sucursal: ${exception.message}"
            }
        }
    }

    /**
     * Resetea el mensaje de error. Llamado por la Activity después de mostrar el Toast.
     * Esto evita que el Toast se repita en cambios de configuración (ej. rotación).
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}