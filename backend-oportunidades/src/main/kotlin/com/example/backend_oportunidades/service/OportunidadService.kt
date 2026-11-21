package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.*
import com.example.backend_oportunidades.error.AccesoDenegadoException
import com.example.backend_oportunidades.error.RecursoNoEncontradoException
import com.example.backend_oportunidades.error.ReglaNegocioException
import com.example.backend_oportunidades.model.*
import com.example.backend_oportunidades.repository.EventoRepository
import com.example.backend_oportunidades.repository.OportunidadRepository
import com.example.backend_oportunidades.repository.SeguimientoRepository
import com.example.backend_oportunidades.repository.UsuarioRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Los permisos se aplican AQUÍ (y no solo en la app), así ninguna pantalla puede saltárselos.
 * Cada operación recibe al usuario que la realiza ([actor]).
 */
@Service
@Transactional
class OportunidadService(
    private val oportunidades: OportunidadRepository,
    private val eventos: EventoRepository,
    private val usuarios: UsuarioRepository,
    private val seguimientos: SeguimientoRepository
) {

    // ---------- Consultas ----------

    /** Supervisor: todas las oportunidades. Ejecutivo: solo las suyas. */
    fun listar(actor: Usuario): List<OportunidadItemDto> {
        val lista = if (Permisos.esSupervisor(actor)) {
            oportunidades.findAll()
        } else {
            oportunidades.findByResponsableIdOrderByFechaActualizacionDesc(actor.id)
        }
        return lista.sortedBy { it.evento.fechaInicio }.map { it.aItemDto() }
    }

    fun obtener(actor: Usuario, id: Long): OportunidadDetalleDto =
        buscarConAcceso(actor, id).aDetalleDto()

    // ---------- Crear y editar ----------

    fun crear(actor: Usuario, solicitud: OportunidadRequest): OportunidadDetalleDto {
        val evento = buscarEvento(solicitud.eventoId)
        validarFechaLimite(solicitud.fechaLimitePostulacion, evento)

        // El supervisor elige al responsable; un ejecutivo siempre crea oportunidades a su nombre
        val responsable = if (Permisos.puedeAsignar(actor)) {
            val elegido = solicitud.responsableId?.let { usuarios.findByIdOrNull(it) }
            if (elegido == null || !elegido.activo || elegido.perfil != Perfil.EJECUTIVO) {
                throw ReglaNegocioException("El responsable debe ser un ejecutivo activo")
            }
            elegido
        } else {
            actor
        }

        // El estado inicial siempre es "Evento identificado" (valor por defecto de la entidad)
        val nueva = Oportunidad(evento = evento, responsable = responsable)
        copiarDatos(nueva, solicitud)
        return oportunidades.save(nueva).aDetalleDto()
    }

    /**
     * Edita los datos de la oportunidad. El estado y el responsable NO se cambian desde aquí
     * (para eso están cambiarEstado y asignar); la prioridad solo la cambia el supervisor.
     */
    fun actualizar(actor: Usuario, id: Long, solicitud: OportunidadRequest): OportunidadDetalleDto {
        val oportunidad = buscarConAcceso(actor, id)
        val evento = buscarEvento(solicitud.eventoId)
        validarFechaLimite(solicitud.fechaLimitePostulacion, evento)

        val prioridadActual = oportunidad.prioridad
        copiarDatos(oportunidad, solicitud)
        if (!Permisos.puedeCambiarPrioridad(actor)) oportunidad.prioridad = prioridadActual
        oportunidad.evento = evento
        oportunidad.fechaActualizacion = System.currentTimeMillis()
        return oportunidades.save(oportunidad).aDetalleDto()
    }

    // ---------- Estados ----------

    /** Cambia el estado respetando permisos y transiciones, y deja constancia en el historial. */
    fun cambiarEstado(actor: Usuario, id: Long, solicitud: CambioEstadoRequest): OportunidadDetalleDto {
        val oportunidad = buscarConAcceso(actor, id)
        val actual = oportunidad.estado
        val nuevo = solicitud.estado

        if (!Permisos.puedeCambiarEstado(actor, oportunidad, nuevo)) {
            if (nuevo in Permisos.estadosSoloSupervisor && !Permisos.esSupervisor(actor)) {
                throw AccesoDenegadoException("Solo un supervisor puede confirmar o descartar una oportunidad")
            }
            throw ReglaNegocioException("No se puede pasar de «${actual.etiqueta()}» a «${nuevo.etiqueta()}»")
        }

        val ahora = System.currentTimeMillis()
        oportunidad.estado = nuevo
        oportunidad.fechaActualizacion = ahora

        // Cada cambio de estado queda en el historial, que nunca se borra
        seguimientos.save(
            Seguimiento(
                oportunidad = oportunidad,
                usuario = actor,
                tipoAccion = when (nuevo) {
                    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> TipoAccion.CONFIRMACION
                    EstadoOportunidad.OPORTUNIDAD_CERRADA -> TipoAccion.CIERRE
                    else -> TipoAccion.SEGUIMIENTO
                },
                fechaHora = ahora,
                resultado = "Cambio de estado: ${actual.etiqueta()} → ${nuevo.etiqueta()}",
                estadoNuevo = nuevo,
                observaciones = solicitud.observacion.trim()
            )
        )
        return oportunidades.save(oportunidad).aDetalleDto()
    }

    // ---------- Asignación (solo supervisor) ----------

    fun asignar(actor: Usuario, id: Long, solicitud: AsignarRequest): OportunidadDetalleDto {
        if (!Permisos.puedeAsignar(actor)) {
            throw AccesoDenegadoException("Solo un supervisor puede asignar oportunidades")
        }
        val oportunidad = buscar(id)

        if (Permisos.esFinal(oportunidad.estado)) {
            throw ReglaNegocioException("No se puede reasignar una oportunidad cerrada o descartada")
        }
        if (oportunidad.responsable.id == solicitud.responsableId) {
            throw ReglaNegocioException("La oportunidad ya está asignada a ese ejecutivo")
        }

        val nuevo = usuarios.findByIdOrNull(solicitud.responsableId)
        if (nuevo == null || !nuevo.activo || nuevo.perfil != Perfil.EJECUTIVO) {
            throw ReglaNegocioException("Solo se puede asignar a un ejecutivo activo")
        }
        val anterior = oportunidad.responsable

        val ahora = System.currentTimeMillis()
        oportunidad.responsable = nuevo
        oportunidad.fechaActualizacion = ahora
        seguimientos.save(
            Seguimiento(
                oportunidad = oportunidad,
                usuario = actor,
                tipoAccion = TipoAccion.SEGUIMIENTO,
                fechaHora = ahora,
                resultado = "Reasignada de ${anterior.nombre} a ${nuevo.nombre}"
            )
        )
        return oportunidades.save(oportunidad).aDetalleDto()
    }

    // ---------- Apoyo ----------

    private fun buscar(id: Long): Oportunidad =
        oportunidades.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("La oportunidad no existe")

    /** Devuelve la oportunidad solo si el usuario tiene permiso para gestionarla. */
    private fun buscarConAcceso(actor: Usuario, id: Long): Oportunidad {
        val oportunidad = buscar(id)
        if (!Permisos.puedeGestionar(actor, oportunidad)) {
            throw AccesoDenegadoException("No tienes acceso a esta oportunidad")
        }
        return oportunidad
    }

    private fun buscarEvento(id: Long): Evento =
        eventos.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("El evento no existe")

    // La fecha límite de postulación no puede ser posterior al evento
    private fun validarFechaLimite(fechaLimite: Long?, evento: Evento) {
        if (fechaLimite != null && fechaLimite > evento.fechaInicio) {
            throw ReglaNegocioException("La fecha límite no puede ser posterior al evento")
        }
    }

    private fun copiarDatos(destino: Oportunidad, solicitud: OportunidadRequest) {
        destino.nombre = solicitud.nombre.trim()
        destino.tipo = solicitud.tipo
        destino.descripcion = solicitud.descripcion.trim()
        destino.fuente = solicitud.fuente.trim()
        destino.prioridad = solicitud.prioridad
        destino.potencial = solicitud.potencial
        destino.afinidad = solicitud.afinidad
        destino.fechaLimitePostulacion = solicitud.fechaLimitePostulacion
        destino.observaciones = solicitud.observaciones.trim()
    }
}