package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.EventoDto
import com.example.backend_oportunidades.dto.EventoRequest
import com.example.backend_oportunidades.service.EventoService
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/eventos")
class EventoController(
    private val eventoService: EventoService,
    private val usuarioService: UsuarioService
) {

    @GetMapping
    fun listar(@RequestHeader("X-Usuario-Id") usuarioId: Long): List<EventoDto> {
        usuarioService.obtenerActor(usuarioId)
        return eventoService.listar()
    }

    @GetMapping("/{id}")
    fun obtener(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ): EventoDto {
        usuarioService.obtenerActor(usuarioId)
        return eventoService.obtener(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @Valid @RequestBody solicitud: EventoRequest
    ): EventoDto {
        usuarioService.obtenerActor(usuarioId)
        return eventoService.crear(solicitud)
    }

    @PutMapping("/{id}")
    fun actualizar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: EventoRequest
    ): EventoDto {
        usuarioService.obtenerActor(usuarioId)
        return eventoService.actualizar(id, solicitud)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ) {
        usuarioService.obtenerActor(usuarioId)
        eventoService.eliminar(id)
    }
}