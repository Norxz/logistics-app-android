package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.model.*

/**
 * Función de extensión que convierte un objeto de respuesta simplificado de la API
 * [SolicitudResponse] al modelo de dominio completo [Solicitud].
 *
 * Esta función es esencial para el desacoplamiento: permite que la capa de red reciba
 * un DTO (Data Transfer Object) simple, mientras que la capa de negocio (ViewModel/UI)
 * trabaja con el modelo de dominio completo, utilizando placeholders cuando es necesario
 * para satisfacer las restricciones de los constructores del modelo.
 *
 * @return Una instancia completa del modelo [Solicitud].
 */
fun SolicitudResponse.toModel(): Solicitud {

    // --- 1. CREACIÓN DE PLACEHOLDERS (Para satisfacer las dependencias obligatorias del modelo) ---

    /**
     * Placeholder para el modelo [User].
     * Se utilizan campos del DTO (clientId) y valores por defecto para satisfacer
     * los campos obligatorios: fullName, email, y role.
     */
    val defaultPlaceholderUser = User(
        id = this.clientId,
        fullName = "Cliente ID: ${this.clientId}",
        email = "cliente${this.clientId}@mail.com",
        role = "CLIENTE"
    )

    /**
     * Placeholder para el modelo [Cliente] (usado como remitente/receptor).
     * Satisface los campos obligatorios: id, nombre, y numeroId.
     */
    val defaultPlaceholderCliente = Cliente(
        id = 0,
        nombre = "N/A",
        numeroId = "0"
    )

    /**
     * Placeholder para el modelo [Direccion].
     * Mapea los campos esenciales recibidos en el DTO (direccionCompleta)
     * y usa nulos o "N/A" para el resto de campos obligatorios/opcionales.
     */
    val defaultPlaceholderDireccion = Direccion(
        direccionCompleta = this.direccionCompleta, // Mapeo del DTO
        ciudad = "N/A",
        latitud = null,
        longitud = null,
        pisoApto = null,
        notasEntrega = null
    )

    /**
     * Placeholder para el modelo [Paquete].
     * Satisface el campo obligatorio 'peso' con un valor de 0.0.
     */
    val defaultPlaceholderPaquete = Paquete(
        peso = 0.0
    )

    /**
     * Mapeo directo del DTO de [GuiaResponse] anidado al modelo [Guia].
     * Se utilizan los campos: numeroGuia, trackingNumber, y fechaCreacion,
     * todos provenientes del DTO anidado (`this.guia`).
     */
    val modelGuia = Guia(
        numeroGuia = this.guia.numeroGuia,
        trackingNumber = this.guia.trackingNumber,
        fechaCreacion = this.guia.fechaCreacion, // Campo opcional que se mapea si existe
        estadoGuia = "ASIGNADA" // Placeholder para el estado inicial
    )

    /**
     * Placeholder para el modelo [Sucursal].
     * Satisface la dependencia obligatoria de [direccion] utilizando el placeholder
     * creado previamente.
     */
    val defaultPlaceholderSucursal = Sucursal(
        id = 0,
        nombre = "N/A",
        direccion = defaultPlaceholderDireccion // Campo obligatorio de Sucursal
    )

    // --- 2. CONSTRUCCIÓN FINAL DEL MODELO SOLICITUD ---

    return Solicitud(
        // Campos directos de SolicitudResponse
        id = this.id,
        fechaRecoleccion = this.fechaRecoleccion,
        franjaHoraria = this.franjaHoraria,
        estado = this.estado,
        createdAt = "N/A", // Timestamp

        // Relaciones Obligatorias (Placeholders y Modelos creados arriba)
        client = defaultPlaceholderUser,
        remitente = defaultPlaceholderCliente,
        receptor = defaultPlaceholderCliente,
        sucursal = defaultPlaceholderSucursal,
        direccion = defaultPlaceholderDireccion,
        paquete = defaultPlaceholderPaquete,
        guia = modelGuia,

        // Relaciones Opcionales (Inicializadas a null)
        conductor = null,
        gestor = null,
        funcionario = null,
        fechaAsignacionConductor = null,
        fechaRecoleccionReal = null,
        fechaEntregaReal = null,
        motivoCancelacion = null
    )
}