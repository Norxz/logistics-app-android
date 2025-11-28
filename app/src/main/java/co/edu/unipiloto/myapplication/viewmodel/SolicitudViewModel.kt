package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.dto.toModel
import kotlinx.coroutines.launch
import java.io.IOException

class SolicitudViewModel(private val repository: SolicitudRepository) : ViewModel() {

    // Los LiveData manejan List<Solicitud>
    private val _activeSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    val activeSolicitudes: LiveData<List<Solicitud>> = _activeSolicitudes

    private val _finishedSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    val finishedSolicitudes: LiveData<List<Solicitud>> = _finishedSolicitudes

    // Estados para la comunicación con la UI
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>(null)
    val actionSuccess: LiveData<String?> = _actionSuccess

    /**
     * Carga todas las solicitudes del cliente y las separa en activas y finalizadas.
     */
    fun loadSolicitudes(userId: Long) {
        viewModelScope.launch {
            try {
                // El repositorio retorna Result<List<SolicitudResponse>>
                val result = repository.getSolicitudesByClient(userId)

                result.fold(
                    onSuccess = { allResponses -> // ⬅️ allResponses es List<SolicitudResponse>

                        // 🚨 CORRECCIÓN: Aplicar el mapeo DTO -> MODELO
                        // Convertimos la lista de DTOs (allResponses) a la lista de Modelos (allSolicitudes)
                        val allSolicitudes: List<Solicitud> = allResponses.map {
                            it.toModel()
                        }

                        // Ahora filtramos la lista de modelos (List<Solicitud>)
                        val activas = allSolicitudes.filter { isSolicitudActiva(it.estado) }
                        val finalizadas = allSolicitudes.filter { !isSolicitudActiva(it.estado) }

                        _activeSolicitudes.value = activas // ✅ Asignación compatible
                        _finishedSolicitudes.value = finalizadas // ✅ Asignación compatible
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
     */
    fun updateSolicitudState(solicitudId: Long, newState: String) {
        viewModelScope.launch {
            try {
                // 🚨 CORRECCIÓN: Llamamos a la función correcta 'updateEstado'
                val result: Result<Unit> = repository.updateEstado(solicitudId, newState)

                // Manejamos el Result<Unit>
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

    fun clearError() {
        _error.value = null
    }

    fun clearActionSuccess() {
        _actionSuccess.value = null
    }
}