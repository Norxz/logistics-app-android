package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import co.edu.unipiloto.myapplication.repository.SucursalRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar los datos y la lógica de negocio de ManageBranchesActivity.
 * Únicamente se encarga de las operaciones relacionadas con las Sucursales.
 */
class ManageBranchesViewModel(private val repository: SucursalRepository) : ViewModel() {

    // --- DATOS OBSERVABLES (LiveData) ---

    // 1. Lista de sucursales que se observará en la Activity (la vista filtrada/actual)
    private val _branches = MutableLiveData<List<SucursalResponse>>()
    val branches: LiveData<List<SucursalResponse>> = _branches

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // --- DATOS INTERNOS ---

    // Copia de todas las sucursales cargadas (para el filtrado sin recargar del API)
    private var allBranches: List<SucursalResponse> = emptyList()

    // --- OPERACIONES ---

    /**
     * Carga todas las sucursales desde el Repositorio de Sucursales.
     */
    fun loadBranches() {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.listarSucursales()

            _loading.value = false
            result.onSuccess { list ->
                // Actualiza la copia de seguridad y la lista visible
                allBranches = list
                _branches.value = list
            }.onFailure { exception ->
                _errorMessage.value = "Error al cargar sucursales: ${exception.message}"
            }
        }
    }

    /**
     * Filtra la lista de sucursales visible basada en el texto de búsqueda (query).
     */
    fun filterBranches(query: String) {
        val filteredList = if (query.isEmpty()) {
            // Si la consulta está vacía, mostrar toda la lista
            allBranches
        } else {
            val lowerCaseQuery = query.lowercase()
            // Filtra por nombre o ciudad (ajusta los campos si es necesario)
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
        // Validación básica
        val id = sucursal.id ?: run {
            _errorMessage.value = "Error: ID de sucursal no disponible."
            return
        }

        _loading.value = true
        viewModelScope.launch {
            val result = repository.eliminarSucursal(id)

            _loading.value = false
            result.onSuccess {
                _errorMessage.value = "Sucursal ${sucursal.nombre} eliminada con éxito."
                // Recarga la lista para reflejar el cambio en la UI
                loadBranches()
            }.onFailure { exception ->
                _errorMessage.value = "Error al eliminar sucursal: ${exception.message}"
            }
        }
    }

    /**
     * Resetea el mensaje de error para evitar repeticiones.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}