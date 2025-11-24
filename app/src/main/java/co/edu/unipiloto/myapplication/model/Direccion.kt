package co.edu.unipiloto.myapplication.model

import java.io.Serializable

data class Direccion(
    val id: Long?,
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double?,
    val longitud: Double?,
    val pisoApto: String?,
    val notasEntrega: String?
)