package co.edu.unipiloto.myapplication.models

/**
 * Modelo de datos para representar a un usuario del personal logístico.
 *
 * @property id ID de la tabla RECOLECTORES (clave primaria para el CRUD de gestión).
 * @property userId ID de la tabla USERS (clave foránea, crucial para actualizar datos en la tabla de autenticación).
 */
data class LogisticUser(
    val id: Long, // ID de la tabla RECOLECTORES
    val email: String,
    val name: String,
    val role: String, // CONDUCTOR, FUNCIONARIO, ANALISTA, GESTOR
    val sucursal: String?, // Zona asignada
    val phoneNumber: String?, // Lo hago opcional ya que en el registro recolector lo dejaste vacío
    val isActive: Boolean,
    val userId: Long? = null // ¡CLAVE FORÁNEA FALTANTE! Resuelve el error de compilación en UserRepository.
)