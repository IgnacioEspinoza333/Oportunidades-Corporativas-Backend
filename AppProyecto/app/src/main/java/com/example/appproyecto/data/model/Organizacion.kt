package com.example.appproyecto.data.model

data class Organizacion(
    val id: Long = 0,
    val nombre: String,
    val tipo: String = TipoOrganizacion.EMPRESA.name,
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)