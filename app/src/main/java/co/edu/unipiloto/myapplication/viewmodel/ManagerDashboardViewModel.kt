package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.model.Solicitud
import co.edu.unipiloto.myapplication.model.User
import co.edu.unipiloto.myapplication.repository.SolicitudRepository
import co.edu.unipiloto.myapplication.repository.UserRepository
import co.edu.unipiloto.myapplication.dto.toModel
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import kotlinx.coroutines.launch
import android.util.Log
import co.edu.unipiloto.myapplication.dto.UserResponse

class ManagerDashboardViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // 🏆 NUEVA PROPIEDAD: Para almacenar el ID de la sucursal actual
    private var currentBranchId: Long? = null

    // --- LiveData de Solicitudes y Estado ---
    private val _pendingSolicitudes = MutableLiveData<List<Solicitud>>(emptyList())
    val pendingSolicitudes: LiveData<List<Solicitud>> = _pendingSolicitudes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // --- LiveData para Gestores y Asignación ---

    // LiveData que guarda la lista de GESTORES disponibles
    private val _availableGestores = MutableLiveData<List<User>>(emptyList())
    // 🏆 CORRECCIÓN 1: Renombrado a 'availableGestores' para coincidir con el Fragmento
    val availableGestores: LiveData<List<User>> = _availableGestores

    // LiveData para el resultado de la asignación
    private val _assignmentResult = MutableLiveData<Result<Solicitud>?>()
    val assignmentResult: LiveData<Result<Solicitud>?> = _assignmentResult


    // --- Funciones de Carga ---

    fun loadBranchSolicitudes(branchId: Long) {
        if (_isLoading.value == true) return
        _isLoading.value = true
        _error.value = null
        currentBranchId = branchId // ✅ Almacenar el ID para futuras recargas

        viewModelScope.launch {
            val result = solicitudRepository.getSolicitudesByBranch(branchId)

            result.onSuccess { dtoList: List<SolicitudResponse> ->
                val modelList: List<Solicitud> = dtoList.map { it.toModel() }
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

    // 🏆 CORRECCIÓN 2: Función renombrada y modificada para usar 'currentBranchId'
    fun loadAvailableGestores() {
        val branchId = currentBranchId ?: run {
            _error.value = "ID de sucursal no establecido para cargar gestores."
            return
        }

        viewModelScope.launch {
            // 🏆 Llamar al endpoint de gestores (conductores activos)
            val result = userRepository.getAvailableManagers(branchId)

            result.onSuccess { gestorList: List<UserResponse> ->
                _availableGestores.value = gestorList.map { it.toModel() }
            }
                .onFailure { exception: Throwable ->
                    _error.value = "Error al cargar gestores disponibles: ${exception.message}"
                    Log.e("ManagerVM", "Error en loadAvailableGestores: ${exception.message}")
                }
        }
    }

    // --- Función de Acción (Asignación) ---

    // Función para asignar un GESTOR a una solicitud
    fun assignGestorToRequest(solicitudId: Long, gestorId: Long) {
        _assignmentResult.value = null // Limpiar resultado anterior
        viewModelScope.launch {
            try {
                // 🚨 Asumimos que solicitudRepository.assignGestor(solicitudId, gestorId) existe
                val result = solicitudRepository.assignGestor(solicitudId, gestorId)

                result.onSuccess { updatedSolicitudResponse: SolicitudResponse ->
                    _assignmentResult.value = Result.success(updatedSolicitudResponse.toModel())

                }.onFailure { exception: Throwable ->
                    _assignmentResult.value = Result.failure(exception)
                }

            } catch (e: Exception) {
                _assignmentResult.value = Result.failure(e)
            }

            // 🏆 CORRECCIÓN 3: Recargar usando el ID de sucursal almacenado
            val branchIdToReload = currentBranchId
            if (branchIdToReload != null) {
                loadBranchSolicitudes(branchIdToReload)
                loadAvailableGestores() // También recargar gestores por si su estado cambió
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}