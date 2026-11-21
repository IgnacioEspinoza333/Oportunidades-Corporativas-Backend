package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.ContactoDto
import com.example.backend_oportunidades.dto.ContactoRequest
import com.example.backend_oportunidades.service.ContactoService
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ContactoController(
    private val contactoService: ContactoService,
    private val usuarioService: UsuarioService
) {

    // Los contactos se listan y se crean "dentro" de una organización
    @GetMapping("/organizaciones/{organizacionId}/contactos")
    fun listarPorOrganizacion(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("organizacionId") organizacionId: Long
    ): List<ContactoDto> {
        usuarioService.obtenerActor(usuarioId)
        return contactoService.listarPorOrganizacion(organizacionId)
    }

    @PostMapping("/organizaciones/{organizacionId}/contactos")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("organizacionId") organizacionId: Long,
        @Valid @RequestBody solicitud: ContactoRequest
    ): ContactoDto {
        usuarioService.obtenerActor(usuarioId)
        return contactoService.crear(organizacionId, solicitud)
    }

    @GetMapping("/contactos/{id}")
    fun obtener(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ): ContactoDto {
        usuarioService.obtenerActor(usuarioId)
        return contactoService.obtener(id)
    }

    @PutMapping("/contactos/{id}")
    fun actualizar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: ContactoRequest
    ): ContactoDto {
        usuarioService.obtenerActor(usuarioId)
        return contactoService.actualizar(id, solicitud)
    }

    @DeleteMapping("/contactos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ) {
        usuarioService.obtenerActor(usuarioId)
        contactoService.eliminar(id)
    }
}