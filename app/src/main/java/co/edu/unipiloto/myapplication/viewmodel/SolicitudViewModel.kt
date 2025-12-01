// ARCHIVO: co.edu.unipiloto.myapplication.viewmodel/SolicitudViewModel.kt

package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.dto.toModel // Asegúrate de que esta extensión exista
import kotlinx.coroutines.launch
import java.io.IOException

// 🏆 NOTA: Este ViewModel SOLO necesita SolicitudRepository
class SolicitudViewModel(private val repository: SolicitudRepository) : ViewModel() {

    // --- DATOS OBSERVABLES ---

    private val _activeSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    // ❌ Error 213: Resuelve 'activeSolicitudes'
    val activeSolicitudes: LiveData<List<Solicitud>> = _activeSolicitudes

    private val _finishedSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    // ❌ Error 219: Resuelve 'finishedSolicitudes'
    val finishedSolicitudes: LiveData<List<Solicitud>> = _finishedSolicitudes

    private val _error = MutableLiveData<String?>(null)
    // ❌ Error 225: Resuelve 'error'
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>(null)
    // ❌ Error 233: Resuelve 'actionSuccess'
    val actionSuccess: LiveData<String?> = _actionSuccess

    // --- FUNCIONES DE LÓGICA DE NEGOCIO ---

    /**
     * Carga todas las solicitudes del cliente y las separa en activas y finalizadas.
     * ❌ Error 94 y 238: Resuelve 'loadSolicitudes'
     */
    fun loadSolicitudes(userId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.getSolicitudesByClient(userId)

                result.fold(
                    onSuccess = { allResponses ->

                        // Mapeo DTO a Modelo para evitar ClassCastException
                        val allSolicitudes: List<Solicitud> = allResponses.map {
                            it.toModel()
                        }

                        val activas = allSolicitudes.filter { isSolicitudActiva(it.estado) }
                        val finalizadas = allSolicitudes.filter { !isSolicitudActiva(it.estado) }

                        _activeSolicitudes.value = activas
                        _finishedSolicitudes.value = finalizadas
                    },
                    onFailure = { e ->
                        _error.value = "Error al cargar solicitudes: ${e.message}"
                    }
                )
            } catch (e: IOException) {
                _error.value = "Error de red al cargar solicitudes."
            }
        }
    }

    /**
     * Llama al repositorio para actualizar el estado de una solicitud.
     * ❌ Error 303: Resuelve 'updateSolicitudState'
     */
    fun updateSolicitudState(solicitudId: Long, newState: String) {
        viewModelScope.launch {
            try {
                val result: Result<Unit> = repository.updateEstado(solicitudId, newState)

                result.fold(
                    onSuccess = {
                        _actionSuccess.value = "Solicitud #${solicitudId} ha sido marcada como $newState."
                    },
                    onFailure = { e ->
                        _error.value = "Error al actualizar estado: ${e.message}"
                    }
                )
            } catch (e: IOException) {
                _error.value = "Error de red al actualizar estado."
            }
        }
    }

    private fun isSolicitudActiva(estado: String): Boolean {
        val estadoUpper = estado.uppercase()
        return estadoUpper !in listOf("ENTREGADA", "FINALIZADA", "CANCELADA")
    }

    // --- FUNCIONES DE UI ---

    /**
     * ❌ Error 228: Resuelve 'clearError'
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * ❌ Error 236: Resuelve 'clearActionSuccess'
     */
    fun clearActionSuccess() {
        _actionSuccess.value = null
    }
}