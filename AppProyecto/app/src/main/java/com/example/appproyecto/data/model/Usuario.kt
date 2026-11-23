package com.example.appproyecto.data.model

data class Usuario(
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val perfil: String = "EJECUTIVO",
    val area: String = "Comercial",
    val zona: String = "Región Metropolitana",
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)