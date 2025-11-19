package co.edu.unipiloto.myapplication.models

import java.io.Serializable

data class Direccion(
    val direccionCompleta: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val pisoApto: String?,
    val notasEntrega: String?,
    val zona: String? // Added 'zona' for the pending requests fragment fix
) : Serializable