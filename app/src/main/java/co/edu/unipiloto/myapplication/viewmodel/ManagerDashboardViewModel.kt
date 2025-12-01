// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/ManagerDashboardViewModel.kt

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.dto.toModel
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import kotlinx.coroutines.launch
import android.util.Log // 🚨 Agregar para loguear errores

class ManagerDashboardViewModel(private val repository: SolicitudRepository) : ViewModel() {

    // --- Pestaña PENDIENTES / SIN ASIGNAR ---

    // LiveData que alimenta el Recycler View de solicitudes Pendientes (no asignadas)
    private val _pendingSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    val pendingSolicitudes: LiveData<List<Solicitud>> = _pendingSolicitudes

    // LiveData para manejar el estado de carga (opcional pero recomendado)
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para manejar errores de la UI
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Carga todas las solicitudes asociadas a la sucursal del Gestor.
     * Luego se filtran en el ViewModel para determinar el estado (PENDIENTE, ASIGNADA, COMPLETADA).
     */
    fun loadBranchSolicitudes(branchId: Long) {
        // Evita cargar si ya está en progreso, o limpia si quieres forzar
        if (_isLoading.value == true) return

        _isLoading.value = true
        _error.value = null // Limpiar error anterior

        viewModelScope.launch {
            // El Repositorio devuelve Result<List<SolicitudResponse>>
            val result = repository.getSolicitudesByBranch(branchId)

            result.onSuccess { dtoList: List<SolicitudResponse> ->

                // Mapear DTO a Modelo
                val modelList: List<Solicitud> = dtoList.map { it.toModel() }

                // Filtrar las pendientes (asumiendo que PENDIENTE es el estado no asignado/inicial)
                // Se podría optimizar llamando a otro endpoint si el backend lo permite
                val pendientes = modelList.filter { it.estado == "PENDIENTE" }

                _pendingSolicitudes.value = pendientes
            }
                .onFailure { exception: Throwable ->
                    val errorMessage = exception.message ?: "Error desconocido en el servidor."
                    _error.value = "Error al cargar solicitudes de la sucursal: $errorMessage"
                    Log.e("ManagerVM", "Error en loadBranchSolicitudes: $errorMessage")
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}