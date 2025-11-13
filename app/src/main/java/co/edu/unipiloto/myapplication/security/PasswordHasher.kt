
package co.edu.unipiloto.myapplication.security

import java.security.MessageDigest

/**
 * Clase estática para generar hashes seguros de contraseñas.
 * Utiliza SHA-256.
 */
object PasswordHasher {

    /**
     * Genera un hash SHA-256 de la contraseña dada.
     * @param password La contraseña en texto plano.
     * @return El hash de la contraseña como una cadena hexadecimal.
     */
    fun hashPassword(password: String): String {
        return try {
            val bytes = password.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)

            // Convierte el array de bytes a una cadena hexadecimal
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // En caso de error (muy raro, solo si el algoritmo no existe)
            // Lanza una excepción o retorna un valor seguro (ej: cadena vacía o nula)
            e.printStackTrace()
            ""
        }
    }
}