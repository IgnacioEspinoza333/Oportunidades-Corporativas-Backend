package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.EventoDto
import com.example.backend_oportunidades.dto.EventoRequest
import com.example.backend_oportunidades.dto.aDto
import com.example.backend_oportunidades.error.ConflictoException
import com.example.backend_oportunidades.error.RecursoNoEncontradoException
import com.example.backend_oportunidades.error.ReglaNegocioException
import com.example.backend_oportunidades.model.Evento
import com.example.backend_oportunidades.model.Organizacion
import com.example.backend_oportunidades.repository.EventoRepository
import com.example.backend_oportunidades.repository.OrganizacionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EventoService(
    private val eventos: EventoRepository,
    private val organizaciones: OrganizacionRepository
) {

    fun listar(): List<EventoDto> = eventos.findAllByOrderByFechaInicio().map { it.aDto() }

    fun obtener(id: Long): EventoDto = buscar(id).aDto()

    fun crear(solicitud: EventoRequest): EventoDto {
        val organizacion = buscarOrganizacion(solicitud.organizacionId)
        val termino = validarFechas(solicitud)
        val nombre = solicitud.nombre.trim()
        if (eventos.existsByNombreIgnoreCaseAndFechaInicioAndOrganizacionId(
                nombre, solicitud.fechaInicio, organizacion.id
            )
        ) {
            throw ConflictoException("Ya existe un evento con ese nombre, fecha y organización")
        }
        val nuevo = Evento(organizacion = organizacion)
        copiarDatos(nuevo, solicitud, organizacion, termino)
        return eventos.save(nuevo).aDto()
    }

    fun actualizar(id: Long, solicitud: EventoRequest): EventoDto {
        val evento = buscar(id)
        val organizacion = buscarOrganizacion(solicitud.organizacionId)
        val termino = validarFechas(solicitud)
        val nombre = solicitud.nombre.trim()
        if (eventos.existsByNombreIgnoreCaseAndFechaInicioAndOrganizacionIdAndIdNot(
                nombre, solicitud.fechaInicio, organizacion.id, id
            )
        ) {
            throw ConflictoException("Ya existe otro evento con ese nombre, fecha y organización")
        }
        copiarDatos(evento, solicitud, organizacion, termino)
        return eventos.save(evento).aDto()
    }

    /** Solo se puede eliminar si ninguna oportunidad nace de él. */
    fun eliminar(id: Long) {
        val evento = buscar(id)
        try {
            eventos.delete(evento)
            eventos.flush()
        } catch (e: DataIntegrityViolationException) {
            throw ConflictoException("No se puede eliminar: el evento tiene oportunidades asociadas")
        }
    }

    // Si no se indica término, el evento dura un solo día. Devuelve la fecha de término final.
    private fun validarFechas(solicitud: EventoRequest): Long {
        val termino = solicitud.fechaTermino ?: solicitud.fechaInicio
        if (termino < solicitud.fechaInicio) {
            throw ReglaNegocioException("La fecha de término no puede ser anterior al inicio")
        }
        return termino
    }

    private fun buscar(id: Long): Evento =
        eventos.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("El evento no existe")

    private fun buscarOrganizacion(id: Long): Organizacion =
        organizaciones.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("La organización no existe")

    private fun copiarDatos(destino: Evento, solicitud: EventoRequest, organizacion: Organizacion, termino: Long) {
        destino.nombre = solicitud.nombre.trim()
        destino.tipo = solicitud.tipo
        destino.descripcion = solicitud.descripcion.trim()
        destino.fechaInicio = solicitud.fechaInicio
        destino.fechaTermino = termino
        destino.horario = solicitud.horario.trim()
        destino.direccion = solicitud.direccion.trim()
        destino.comuna = solicitud.comuna.trim()
        destino.region = solicitud.region.trim()
        destino.organizacion = organizacion
        destino.publicoEsperado = solicitud.publicoEsperado
        destino.publicoObjetivo = solicitud.publicoObjetivo.trim()
        destino.condiciones = solicitud.condiciones.trim()
        destino.costoReferencial = solicitud.costoReferencial
        destino.observaciones = solicitud.observaciones.trim()
    }
}