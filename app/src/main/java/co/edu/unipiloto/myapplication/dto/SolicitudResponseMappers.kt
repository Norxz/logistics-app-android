// co.edu.unipiloto.myapplication.dto.SolicitudResponseMappers.kt
package co.edu.unipiloto.myapplication.dto

import co.edu.unipiloto.myapplication.model.*

/**
 * Función de extensión que convierte SolicitudResponse al modelo de dominio Solicitud.
 * Usa PLACEHOLDERS para las relaciones OBLIGATORIAS que el DTO simplificado no trae.
 */
fun SolicitudResponse.toModel(): Solicitud {

    // 1.0 FUNCIÓN AUXILIAR GENÉRICA PARA CREAR USER PLACEHOLDERS
    // Utilizamos 'fullName' para el modelo User.
    val createUserPlaceholder = { id: Long?, name: String, role: String ->
        User(
            id = id,
            fullName = name, // 🚨 CORREGIDO: Usa 'fullName' para el modelo User
            email = "${name.replace(" ", "")}@mail.com",
            role = role
        )
    }

    // --- 1.1 CREACIÓN DE PLACEHOLDERS ESPECÍFICOS Y ASIGNACIÓN DE CONDUCTOR ---

    /** Placeholder para el modelo [User] (Cliente). */
    val clientUser = createUserPlaceholder(
        this.clientId,
        "Cliente ID: ${this.clientId}",
        "CLIENTE"
    )

    /** Crea el objeto User para el CONDUCTOR ASIGNADO (si existe). */
    val conductorUser: User? = if (this.recolectorId != null) {
        createUserPlaceholder(
            this.recolectorId,
            this.recolectorName ?: "Conductor Asignado",
            "CONDUCTOR"
        )
    } else {
        null
    }

    // Placeholders para cumplir con las relaciones OBLIGATORIAS del modelo Solicitud:

    // Objeto Direccion: Asumimos que los campos no conocidos son null.
    val defaultPlaceholderDireccion = Direccion(
        direccionCompleta = this.direccionCompleta,
        ciudad = "N/A",
        latitud = null,
        longitud = null,
        pisoApto = null,
        notasEntrega = null
    )

    /** Cliente Placeholder (para remitente y receptor OBLIGATORIOS). */
    val defaultPlaceholderCliente = Cliente(
        id = 0,
        nombre = "N/A", // 🚨 CORREGIDO: Usamos 'nombre' para el modelo Cliente
        numeroId = "0"
    )

    /** Paquete Placeholder OBLIGATORIO. */
    val defaultPlaceholderPaquete = Paquete(
        peso = 0.0 // 🚨 CORREGIDO: Usamos 'peso' para el modelo Paquete
    )

    /** Guia Mapeada (OBLIGATORIA). */
    // 🚨 CORREGIDO: Usamos los nombres de campos del modelo Guia
    val modelGuia = Guia(
        numeroGuia = this.guia.numeroGuia,
        trackingNumber = this.guia.trackingNumber,
        fechaCreacion = this.guia.fechaCreacion,
        estadoGuia = "ASIGNADA"
    )

    /** Sucursal Placeholder OBLIGATORIA. */
    val defaultPlaceholderSucursal = Sucursal(
        id = 0,
        nombre = "N/A",
        direccion = defaultPlaceholderDireccion // 🚨 CORREGIDO: Sucursal requiere un objeto 'direccion'
    )

    // --- 2. CONSTRUCCIÓN FINAL DEL MODELO SOLICITUD ---

    return Solicitud(
        // Campos directos del DTO
        id = this.id,
        fechaRecoleccion = this.fechaRecoleccion,
        franjaHoraria = this.franjaHoraria,
        estado = this.estado,
        createdAt = this.createdAt ?: "N/A",

        // Relaciones Obligatorias (Placeholders)
        client = clientUser,
        remitente = defaultPlaceholderCliente,
        receptor = defaultPlaceholderCliente,
        sucursal = defaultPlaceholderSucursal,
        direccionRecoleccion = defaultPlaceholderDireccion,
        direccionEntrega = defaultPlaceholderDireccion,
        paquete = defaultPlaceholderPaquete,
        guia = modelGuia,

        // Asignación de Conductor
        conductor = conductorUser,

        // Opcionales (Inicializados a null)
        gestor = null,
        funcionario = null,
        fechaAsignacionConductor = null,
        fechaRecoleccionReal = null,
        fechaEntregaReal = null,
        motivoCancelacion = null
    )
}