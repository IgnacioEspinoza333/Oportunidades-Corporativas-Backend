package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.SeguimientoItemDto
import com.example.backend_oportunidades.dto.SeguimientoPendienteDto
import com.example.backend_oportunidades.dto.SeguimientoRequest
import com.example.backend_oportunidades.service.SeguimientoService
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SeguimientoController(
    private val seguimientoService: SeguimientoService,
    private val usuarioService: UsuarioService
) {

    // El historial "cuelga" de una oportunidad
    @GetMapping("/oportunidades/{id}/seguimientos")
    fun historial(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ): List<SeguimientoItemDto> =
        seguimientoService.historial(usuarioService.obtenerActor(usuarioId), id)

    @PostMapping("/oportunidades/{id}/seguimientos")
    @ResponseStatus(HttpStatus.CREATED)
    fun registrar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: SeguimientoRequest
    ): SeguimientoItemDto =
        seguimientoService.registrar(usuarioService.obtenerActor(usuarioId), id, solicitud)

    // Próximas gestiones programadas (alimenta las alertas del inicio de la app)
    @GetMapping("/seguimientos/pendientes")
    fun pendientes(@RequestHeader("X-Usuario-Id") usuarioId: Long): List<SeguimientoPendienteDto> =
        seguimientoService.pendientes(usuarioService.obtenerActor(usuarioId))
}