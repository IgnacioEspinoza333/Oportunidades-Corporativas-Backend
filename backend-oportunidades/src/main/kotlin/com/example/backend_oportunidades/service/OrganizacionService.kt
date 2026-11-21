package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.OrganizacionDto
import com.example.backend_oportunidades.dto.OrganizacionRequest
import com.example.backend_oportunidades.dto.aDto
import com.example.backend_oportunidades.error.ConflictoException
import com.example.backend_oportunidades.error.RecursoNoEncontradoException
import com.example.backend_oportunidades.model.Organizacion
import com.example.backend_oportunidades.repository.OrganizacionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrganizacionService(private val organizaciones: OrganizacionRepository) {

    fun listar(): List<OrganizacionDto> =
        organizaciones.findAll(Sort.by("nombre")).map { it.aDto() }

    fun obtener(id: Long): OrganizacionDto = buscar(id).aDto()

    fun crear(solicitud: OrganizacionRequest): OrganizacionDto {
        val nombre = solicitud.nombre.trim()
        if (organizaciones.existsByNombreIgnoreCase(nombre)) {
            throw ConflictoException("Ya existe una organización con ese nombre")
        }
        val nueva = Organizacion()
        copiarDatos(nueva, solicitud)
        return organizaciones.save(nueva).aDto()
    }

    fun actualizar(id: Long, solicitud: OrganizacionRequest): OrganizacionDto {
        val organizacion = buscar(id)
        val nombre = solicitud.nombre.trim()
        if (organizaciones.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw ConflictoException("Ya existe otra organización con ese nombre")
        }
        copiarDatos(organizacion, solicitud)
        return organizaciones.save(organizacion).aDto()
    }

    /** Solo se puede eliminar si no tiene contactos ni eventos asociados. */
    fun eliminar(id: Long) {
        val organizacion = buscar(id)
        try {
            organizaciones.delete(organizacion)
            organizaciones.flush() // fuerza el borrado ahora, para detectar si la base lo rechaza
        } catch (e: DataIntegrityViolationException) {
            throw ConflictoException("No se puede eliminar: la organización tiene contactos o eventos asociados")
        }
    }

    private fun buscar(id: Long): Organizacion =
        organizaciones.findByIdOrNull(id) ?: throw RecursoNoEncontradoException("La organización no existe")

    private fun copiarDatos(destino: Organizacion, solicitud: OrganizacionRequest) {
        destino.nombre = solicitud.nombre.trim()
        destino.tipo = solicitud.tipo
        destino.rubro = solicitud.rubro.trim()
        destino.comuna = solicitud.comuna.trim()
        destino.region = solicitud.region.trim()
        destino.sitioWeb = solicitud.sitioWeb.trim()
        destino.publicoAsociado = solicitud.publicoAsociado.trim()
        destino.observaciones = solicitud.observaciones.trim()
    }
}