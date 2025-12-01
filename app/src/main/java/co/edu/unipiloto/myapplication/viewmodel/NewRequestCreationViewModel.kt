// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/NewRequestCreationViewModel.kt (Corregido)

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.dto.SolicitudRequest
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.dto.SucursalResponse // ⬅️ IMPORTAR SucursalResponse
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.SucursalRepository
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel dedicado a la lógica de Creación de una nueva solicitud (cliente).
 */
class NewRequestCreationViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val sucursalRepository: SucursalRepository
) : ViewModel() {

    private val _saveResult = MutableLiveData<Result<SolicitudResponse>>()
    val saveResult: LiveData<Result<SolicitudResponse>> = _saveResult

    /**
     * Procesa la solicitud encontrando la sucursal más cercana y luego la guarda.
     * @param request La SolicitudRequest inicial sin el ID de sucursal.
     * @param recLat Latitud de recolección.
     * @param recLon Longitud de recolección.
     */
    fun processAndSaveSolicitud(request: SolicitudRequest, recLat: Double, recLon: Double) {
        viewModelScope.launch {
            try {
                // 1. Buscar la sucursal más cercana
                val sucursalResult: Result<SucursalResponse> = sucursalRepository.findNearestSucursal(recLat, recLon)

                sucursalResult.onSuccess { sucursal: SucursalResponse ->

                    // 2. Actualizar la solicitud con el ID de la sucursal encontrada
                    // ✅ CORRECCIÓN: sucursal.id ya está en el objeto de nivel superior (SucursalResponse)
                    val finalRequest = request.copy(sucursalId = sucursal.id)

                    // 3. Guardar la solicitud final en el backend
                    val saveResponseResult = solicitudRepository.crearSolicitud(finalRequest)

                    _saveResult.value = saveResponseResult
                }.onFailure { exception: Throwable ->
                    // Si falla la búsqueda de sucursal, reportar el error
                    _saveResult.value = Result.failure(Exception("Error al buscar sucursal: ${exception.message}"))
                }

            } catch (e: Exception) {
                _saveResult.value = Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
}