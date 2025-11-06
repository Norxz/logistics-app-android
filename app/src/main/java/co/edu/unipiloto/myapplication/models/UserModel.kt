package co.edu.unipiloto.myapplication.models

/**
 * Modelo unificado para datos de usuario/personal logístico tras el login.
 *
 * @param id El ID único en su tabla respectiva (users o recolectores).
 * @param role El rol del usuario (CLIENTE, CONDUCTOR, GESTOR, etc.).
 * @param zona La zona de operación (solo aplica a personal logístico).
 */
data class UserModel(
    val id: Long,
    val role: String,
    val zona: String? // Nullable si es CLIENTE
)