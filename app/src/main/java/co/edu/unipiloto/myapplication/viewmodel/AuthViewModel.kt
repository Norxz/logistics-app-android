package co.edu.unipiloto.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unipiloto.myapplication.dto.LoginRequest
import co.edu.unipiloto.myapplication.dto.RegisterRequest
import co.edu.unipiloto.myapplication.dto.UserResponse
import co.edu.unipiloto.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * 💡 ViewModel de Autenticación.
 * Contiene la lógica de negocio y mantiene el estado de la UI (AuthState).
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Estado del proceso de autenticación (Éxito, Error, Reposo)
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    // Estado de carga (para el ProgressBar)
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Inicia el proceso de Login.
     */
    fun login(request: LoginRequest) {
        _isLoading.value = true
        // 🚀 Lanza la Coroutine en el scope del ViewModel
        viewModelScope.launch {
            val result = repository.login(request)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onFailure = { throwable ->
                    val errorMessage = when (throwable) {
                        is IOException -> "Error de red. Verifica tu conexión a Internet."
                        else -> throwable.message ?: "Error desconocido durante el login."
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            )
        }
    }

    /**
     * Inicia el proceso de Registro.
     */
    fun register(request: RegisterRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(request)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onFailure = { throwable ->
                    val errorMessage = when (throwable) {
                        is IOException -> "Error de red. Verifica tu conexión a Internet."
                        else -> throwable.message ?: "Error desconocido durante el registro."
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            )
        }
    }

    /**
     * Limpia el estado de la autenticación después de un manejo.
     */
    fun clearState() {
        _authState.value = AuthState.Idle
    }
}

/**
 * 🧩 Estados Sellados (Sealed Class) para representar el resultado de la autenticación.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data class Success(val user: UserResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}