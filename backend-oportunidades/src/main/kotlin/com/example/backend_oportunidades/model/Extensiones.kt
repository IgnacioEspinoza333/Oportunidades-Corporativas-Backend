package com.example.backend_oportunidades.model

/** Nombre legible del estado (es el mismo texto que muestra la app). */
fun EstadoOportunidad.etiqueta(): String = when (this) {
    EstadoOportunidad.EVENTO_IDENTIFICADO -> "Evento identificado"
    EstadoOportunidad.CONTACTO_PENDIENTE -> "Contacto pendiente"
    EstadoOportunidad.CONTACTO_REALIZADO -> "Contacto realizado"
    EstadoOportunidad.PROPUESTA_ENVIADA -> "Propuesta enviada"
    EstadoOportunidad.EN_ESPERA_RESPUESTA -> "En espera de respuesta"
    EstadoOportunidad.EN_EVALUACION -> "En evaluación"
    EstadoOportunidad.REQUIERE_SEGUIMIENTO -> "Requiere seguimiento"
    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> "Participación confirmada"
    EstadoOportunidad.ACTIVACION_REALIZADA -> "Activación realizada"
    EstadoOportunidad.ACTIVACION_CANCELADA -> "Activación cancelada"
    EstadoOportunidad.OPORTUNIDAD_DESCARTADA -> "Oportunidad descartada"
    EstadoOportunidad.OPORTUNIDAD_CERRADA -> "Oportunidad cerrada"
}

/** Acciones que implican hablar con alguien: deben registrar con qué contacto. */
val TipoAccion.requiereContacto: Boolean
    get() = when (this) {
        TipoAccion.PRIMER_CONTACTO, TipoAccion.ENVIO_PRESENTACION, TipoAccion.ENVIO_PROPUESTA,
        TipoAccion.LLAMADA, TipoAccion.REUNION, TipoAccion.RECEPCION_RESPUESTA -> true
        else -> false
    }