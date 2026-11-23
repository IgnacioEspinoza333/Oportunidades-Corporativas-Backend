package com.example.appproyecto.data.model

data class Oportunidad(
    val id: Long = 0,
    val nombre: String,
    val tipo: String = TipoOportunidad.EVENTO_CORPORATIVO.name,
    val descripcion: String = "",
    val fuente: String = "",
    val fechaDeteccion: Long = System.currentTimeMillis(),
    val eventoId: Long,
    val responsableId: Long,
    val prioridad: String = Prioridad.MEDIA.name,
    val potencial: Int = 3,
    val afinidad: Int = 3,
    val estado: String = EstadoOportunidad.EVENTO_IDENTIFICADO.name,
    val fechaLimitePostulacion: Long? = null,
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val observaciones: String = ""
)