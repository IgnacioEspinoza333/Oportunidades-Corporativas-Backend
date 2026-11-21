package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.model.EstadoOportunidad
import com.example.backend_oportunidades.model.EstadoOportunidad.*
import com.example.backend_oportunidades.model.Oportunidad
import com.example.backend_oportunidades.model.Perfil
import com.example.backend_oportunidades.model.Usuario

/** Todas las reglas de "quién puede qué" en un solo lugar. */
object Permisos {

    // ---------- Roles ----------

    fun esSupervisor(usuario: Usuario): Boolean =
        usuario.activo && usuario.perfil == Perfil.SUPERVISOR

    // ---------- Acceso a oportunidades ----------

    /** El supervisor ve todo; el ejecutivo solo las oportunidades donde es responsable. */
    fun puedeVer(usuario: Usuario, oportunidad: Oportunidad): Boolean =
        usuario.activo && (esSupervisor(usuario) || oportunidad.responsable.id == usuario.id)

    /** Registrar seguimientos, editar datos y proponer cambios de estado. */
    fun puedeGestionar(usuario: Usuario, oportunidad: Oportunidad): Boolean =
        puedeVer(usuario, oportunidad)

    // ---------- Acciones exclusivas del supervisor ----------

    fun puedeAsignar(usuario: Usuario): Boolean = esSupervisor(usuario)

    fun puedeCambiarPrioridad(usuario: Usuario): Boolean = esSupervisor(usuario)

    // ---------- Estados y transiciones ----------

    /** Estados finales: desde aquí un ejecutivo ya no puede avanzar la oportunidad. */
    fun esFinal(estado: EstadoOportunidad): Boolean =
        estado == OPORTUNIDAD_DESCARTADA || estado == OPORTUNIDAD_CERRADA

    /** Confirmar o descartar una participación es decisión del supervisor. */
    val estadosSoloSupervisor: Set<EstadoOportunidad> =
        setOf(PARTICIPACION_CONFIRMADA, OPORTUNIDAD_DESCARTADA)

    /** Flujo normal de una oportunidad: de qué estado se puede pasar a cuáles. */
    private val flujo: Map<EstadoOportunidad, Set<EstadoOportunidad>> = mapOf(
        EVENTO_IDENTIFICADO to setOf(CONTACTO_PENDIENTE),
        CONTACTO_PENDIENTE to setOf(CONTACTO_REALIZADO),
        CONTACTO_REALIZADO to setOf(PROPUESTA_ENVIADA, REQUIERE_SEGUIMIENTO),
        PROPUESTA_ENVIADA to setOf(EN_ESPERA_RESPUESTA, REQUIERE_SEGUIMIENTO),
        EN_ESPERA_RESPUESTA to setOf(EN_EVALUACION, REQUIERE_SEGUIMIENTO),
        // "En evaluación" = esperando la decisión del supervisor.
        EN_EVALUACION to setOf(PARTICIPACION_CONFIRMADA, OPORTUNIDAD_DESCARTADA, REQUIERE_SEGUIMIENTO),
        REQUIERE_SEGUIMIENTO to setOf(CONTACTO_REALIZADO, PROPUESTA_ENVIADA, EN_ESPERA_RESPUESTA, EN_EVALUACION),
        PARTICIPACION_CONFIRMADA to setOf(ACTIVACION_REALIZADA, ACTIVACION_CANCELADA),
        ACTIVACION_REALIZADA to setOf(OPORTUNIDAD_CERRADA),
        ACTIVACION_CANCELADA to setOf(OPORTUNIDAD_CERRADA),
        OPORTUNIDAD_DESCARTADA to emptySet(),
        OPORTUNIDAD_CERRADA to emptySet()
    )

    /** Estados a los que puede pasar una oportunidad que hoy está en [actual], según el rol. */
    fun estadosDestino(rol: Perfil, actual: EstadoOportunidad): List<EstadoOportunidad> {
        val base = flujo[actual].orEmpty()
        val permitidos = when (rol) {
            Perfil.EJECUTIVO -> base - estadosSoloSupervisor
            Perfil.SUPERVISOR -> when {
                // Puede reabrir una oportunidad descartada...
                actual == OPORTUNIDAD_DESCARTADA -> setOf(EN_EVALUACION)
                // ...y descartar en cualquier momento mientras siga abierta.
                !esFinal(actual) -> base + OPORTUNIDAD_DESCARTADA
                else -> base
            }
        }
        return permitidos.toList()
    }

    fun puedeCambiarEstado(usuario: Usuario, oportunidad: Oportunidad, destino: EstadoOportunidad): Boolean {
        if (!puedeGestionar(usuario, oportunidad)) return false
        return destino in estadosDestino(usuario.perfil, oportunidad.estado)
    }
}