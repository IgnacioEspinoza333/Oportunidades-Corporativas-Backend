package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.SeguimientoItemDto
import com.example.backend_oportunidades.dto.SeguimientoPendienteDto
import com.example.backend_oportunidades.dto.SeguimientoRequest
import com.example.backend_oportunidades.dto.aItemDto
import com.example.backend_oportunidades.error.AccesoDenegadoException
import com.example.backend_oportunidades.error.RecursoNoEncontradoException
import com.example.backend_oportunidades.error.ReglaNegocioException
import com.example.backend_oportunidades.model.Oportunidad
import com.example.backend_oportunidades.model.Seguimiento
import com.example.backend_oportunidades.model.Usuario
import com.example.backend_oportunidades.model.requiereContacto
import com.example.backend_oportunidades.repository.ContactoRepository
import com.example.backend_oportunidades.repository.OportunidadRepository
import com.example.backend_oportunidades.repository.SeguimientoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SeguimientoService(
    private val seguimientos: SeguimientoRepository,
    private val oportunidades: OportunidadRepository,
    private val contactos: ContactoRepository
) {

    /** Historial de gestiones de una oportunidad, de la más reciente a la más antigua. */
    fun historial(actor: Usuario, oportunidadId: Long): List<SeguimientoItemDto> {
        val oportunidad = buscarConAcceso(actor, oportunidadId)
        return historialOrdenado(oportunidad.id).map { it.aItemDto() }
    }

    /**
     * Registra una gestión en el historial. Quien la registra es siempre el [actor] y la fecha es
     * la actual. Los cambios de estado NO se hacen aquí: para eso está OportunidadService.
     */
    fun registrar(actor: Usuario, oportunidadId: Long, solicitud: SeguimientoRequest): SeguimientoItemDto {
        val oportunidad = buscarConAcceso(actor, oportunidadId)

        if (Permisos.esFinal(oportunidad.estado)) {
            throw ReglaNegocioException("La oportunidad está cerrada o descartada y no admite nuevas gestiones")
        }

        val resultado = solicitud.resultado.trim()
        if (resultado.length < 5) {
            throw ReglaNegocioException("El resultado debe tener al menos 5 caracteres")
        }

        // La próxima acción y su fecha van juntas: si hay una, debe haber la otra
        val proximaAccion = solicitud.proximaAccion.trim()
        if (solicitud.fechaProximoSeguimiento != null && proximaAccion.isEmpty()) {
            throw ReglaNegocioException("Indica cuál será la próxima acción")
        }
        if (solicitud.fechaProximoSeguimiento == null && proximaAccion.isNotEmpty()) {
            throw ReglaNegocioException("Selecciona la fecha del próximo seguimiento")
        }

        // Las acciones que implican hablar con alguien exigen un contacto de la organización del evento
        val contacto = if (solicitud.tipoAccion.requiereContacto) {
            val idContacto = solicitud.contactoId
                ?: throw ReglaNegocioException("Selecciona con qué contacto se realizó la gestión")
            val encontrado = contactos.findByIdOrNull(idContacto)
                ?: throw RecursoNoEncontradoException("El contacto no existe")
            if (encontrado.organizacion.id != oportunidad.evento.organizacion.id) {
                throw ReglaNegocioException("El contacto no pertenece a la organización del evento")
            }
            encontrado
        } else {
            null
        }

        val ahora = System.currentTimeMillis()
        val guardado = seguimientos.save(
            Seguimiento(
                oportunidad = oportunidad,
                usuario = actor,
                contacto = contacto,
                tipoAccion = solicitud.tipoAccion,
                fechaHora = ahora,
                resultado = resultado,
                proximaAccion = proximaAccion,
                fechaProximoSeguimiento = solicitud.fechaProximoSeguimiento,
                estadoNuevo = null,
                observaciones = solicitud.observaciones.trim()
            )
        )
        oportunidad.fechaActualizacion = ahora
        oportunidades.save(oportunidad)
        return guardado.aItemDto()
    }

    /**
     * Próxima gestión programada de cada oportunidad abierta: la de la última gestión que dejó
     * una fecha. El supervisor ve las de todos; el ejecutivo, solo las suyas.
     */
    fun pendientes(actor: Usuario): List<SeguimientoPendienteDto> {
        val visibles = if (Permisos.esSupervisor(actor)) {
            oportunidades.findAll()
        } else {
            oportunidades.findByResponsableIdOrderByFechaActualizacionDesc(actor.id)
        }

        return visibles
            .filter { !Permisos.esFinal(it.estado) }
            .mapNotNull { oportunidad ->
                val ultimo = historialOrdenado(oportunidad.id).firstOrNull { it.fechaProximoSeguimiento != null }
                    ?: return@mapNotNull null
                val fecha = ultimo.fechaProximoSeguimiento ?: return@mapNotNull null
                SeguimientoPendienteDto(
                    oportunidadId = oportunidad.id,
                    oportunidadNombre = oportunidad.nombre,
                    estado = oportunidad.estado,
                    responsableId = oportunidad.responsable.id,
                    responsableNombre = oportunidad.responsable.nombre,
                    proximaAccion = ultimo.proximaAccion,
                    fecha = fecha
                )
            }
            .sortedBy { it.fecha }
    }

    // ---------- Apoyo ----------

    // Más reciente primero; si dos tienen la misma hora, gana el que se creó después
    private fun historialOrdenado(oportunidadId: Long): List<Seguimiento> =
        seguimientos.findByOportunidadIdOrderByFechaHoraDesc(oportunidadId)
            .sortedWith(compareByDescending<Seguimiento> { it.fechaHora }.thenByDescending { it.id })

    private fun buscarConAcceso(actor: Usuario, id: Long): Oportunidad {
        val oportunidad = oportunidades.findByIdOrNull(id)
            ?: throw RecursoNoEncontradoException("La oportunidad no existe")
        if (!Permisos.puedeGestionar(actor, oportunidad)) {
            throw AccesoDenegadoException("No tienes acceso a esta oportunidad")
        }
        return oportunidad
    }
}