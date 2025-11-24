package co.edu.unipiloto.myapplication.model

data class Sucursal(
    val id: Long,
    val nombre: String,
    val direccion: Direccion? = null
)