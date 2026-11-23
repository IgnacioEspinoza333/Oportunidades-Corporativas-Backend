package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Seguimiento

/** Datos de una gestión tal como los espera el backend (quién la registra y la fecha los pone el servidor). */
fun Seguimiento.aRequest() = SeguimientoRequest(
    contactoId = contactoId,
    tipoAccion = tipoAccion,
    resultado = resultado,
    proximaAccion = proximaAccion,
    fechaProximoSeguimiento = fechaProximoSeguimiento,
    observaciones = observaciones
)