package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.ContactoDto
import com.example.backend_oportunidades.dto.ContactoRequest
import com.example.backend_oportunidades.dto.aDto
import com.example.backend_oportunidades.error.ConflictoException
import com.example.backend_oportunidades.error.RecursoNoEncontradoException
import com.example.backend_oportunidades.model.Contacto
import com.example.backend_oportunidades.model.Organizacion
import com.example.backend_oportunidades.repository.ContactoRepository
import com.example.backend_oportunidades.repository.OrganizacionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContactoService(
    private val contactos: ContactoRepository,
    private val organizaciones: OrganizacionRepository
) {

    fun listarPorOrganizacion(organizacionId: Long): List<ContactoDto> {
        buscarOrganizacion(organizacionId) // si la organización no existe, responde 404
        return contactos.findByOrganizacionIdOrderByNombre(organizacionId).map { it.aDto() }
    }

    fun obtener(id: Long): ContactoDto = buscar(id).aDto()

    fun crear(organizacionId: Long, solicitud: ContactoRequest): ContactoDto {
        val organizacion = buscarOrganizacion(organizacionId)
        val correo = solicitud.correo.trim().lowercase()
        if (contactos.existsByOrganizacionIdAndCorreoIgnoreCase(organizacionId, correo)) {
            throw ConflictoException("Ya existe un contacto con ese correo en esta organización")
        }
        val nuevo = Contacto(organizacion = organizacion)
        copiarDatos(nuevo, solicitud, correo)
        return contactos.save(nuevo).aDto()
    }

    // La organización y la fecha del último contacto no se cambian desde aquí.
    fun actualizar(id: Long, solicitud: ContactoRequest): ContactoDto {
        val contacto = buscar(id)
        val correo = solicitud.correo.trim().lowercase()
        if (contactos.existsByOrganizacionIdAndCorreoIgnoreCaseAndIdNot(contacto.organizacion.id, correo, id)) {
            throw ConflictoException("Ya existe otro contacto con ese correo en esta organización")
        }
        copiarDatos(contacto, solicitud, correo)
        return contactos.save(contacto).aDto()
    }

    /** Solo se puede eliminar si ningún seguimiento lo usa. */
    fun eliminar(id: Long) {
        val contacto = buscar(id)
        try {
            contactos.delete(contacto)
            contactos.flush()
        } catch (e: DataIntegrityViolationException) {
            throw ConflictoException("No se puede eliminar: el contacto tiene seguimientos asociados")
        }
    }

    private fun buscar(id: Long): Contacto =
        contactos.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("El contacto no existe")

    private fun buscarOrganizacion(id: Long): Organizacion =
        organizaciones.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("La organización no existe")

    private fun copiarDatos(destino: Contacto, solicitud: ContactoRequest, correo: String) {
        destino.nombre = solicitud.nombre.trim()
        destino.cargo = solicitud.cargo.trim()
        destino.area = solicitud.area
        destino.correo = correo
        destino.telefono = solicitud.telefono.trim()
        destino.medioPreferente = solicitud.medioPreferente.trim()
        destino.estado = solicitud.estado.trim()
        destino.observaciones = solicitud.observaciones.trim()
    }
}