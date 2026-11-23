package com.example.appproyecto.data.model

/**
 * Una gestión realizada sobre una oportunidad (llamada, envío de propuesta, reunión...).
 * Los registros nunca se borran: forman el historial de la oportunidad.
 */
data class Seguimiento(
    val id: Long = 0,
    val oportunidadId: Long,
    /** Quién registró la gestión. */
    val usuarioId: Long,
    /** Contacto con el que se habló (opcional). */
    val contactoId: Long? = null,
    val tipoAccion: String = TipoAccion.SEGUIMIENTO.name,
    val fechaHora: Long = System.currentTimeMillis(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null,
    /** Si esta gestión cambió el estado de la oportunidad, queda registrado aquí. */
    val estadoNuevo: String? = null,
    val observaciones: String = ""
)