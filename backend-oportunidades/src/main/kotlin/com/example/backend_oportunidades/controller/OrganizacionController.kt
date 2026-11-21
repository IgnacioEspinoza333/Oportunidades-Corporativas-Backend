package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.OrganizacionDto
import com.example.backend_oportunidades.dto.OrganizacionRequest
import com.example.backend_oportunidades.service.OrganizacionService
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizaciones")
class OrganizacionController(
    private val organizacionService: OrganizacionService,
    private val usuarioService: UsuarioService
) {

    // En todos los métodos, obtenerActor comprueba que quien llama existe y está activo
    @GetMapping
    fun listar(@RequestHeader("X-Usuario-Id") usuarioId: Long): List<OrganizacionDto> {
        usuarioService.obtenerActor(usuarioId)
        return organizacionService.listar()
    }

    @GetMapping("/{id}")
    fun obtener(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ): OrganizacionDto {
        usuarioService.obtenerActor(usuarioId)
        return organizacionService.obtener(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @Valid @RequestBody solicitud: OrganizacionRequest
    ): OrganizacionDto {
        usuarioService.obtenerActor(usuarioId)
        return organizacionService.crear(solicitud)
    }

    @PutMapping("/{id}")
    fun actualizar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: OrganizacionRequest
    ): OrganizacionDto {
        usuarioService.obtenerActor(usuarioId)
        return organizacionService.actualizar(id, solicitud)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ) {
        usuarioService.obtenerActor(usuarioId)
        organizacionService.eliminar(id)
    }
}