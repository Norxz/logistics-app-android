// ARCHIVO: co.edu.unipiloto.myapplication.dto/UserMappers.kt

package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.model.Sucursal
import co.edu.unipiloto.myapplication.model.User
// Debes importar la clase modelo Direccion si el DTO no está en el mismo paquete
// import co.edu.unipiloto.myapplication.model.Direccion

/**
 * Mapeador de DTO de Sucursal a Modelo de Dominio.
 * @return El modelo de Sucursal.
 */
fun SucursalResponse.toModel(): Sucursal {
    return Sucursal(
        id = this.id,
        nombre = this.nombre,
        // La propiedad 'direccion' ya es del tipo modelo Direccion en el DTO,
        // por lo que se pasa directamente.
        direccion = this.direccion
    )
}

/**
 * Mapeador de DTO de Usuario a Modelo de Dominio.
 * Resuelve el error de tipo en el ViewModel.
 * @return El modelo de User.
 */
fun UserResponse.toModel(): User {
    return User(
        id = this.id,
        // 🏆 CORRECCIÓN DE NOMBRES: Usamos 'fullName' y 'role' del DTO.
        fullName = this.fullName,
        email = this.email,
        role = this.role,
        // 🏆 CORRECCIÓN DE TIPO: Mapeamos el DTO SucursalResponse a Sucursal.
        sucursal = this.sucursal?.toModel()
        // Nota: phoneNumber e isActive se omiten si no están en el constructor de User
        // Si User requiere phoneNumber e isActive, añádelos aquí.
    )
}