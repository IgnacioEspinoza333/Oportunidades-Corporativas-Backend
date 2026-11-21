package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.AsignarRequest
import com.example.backend_oportunidades.dto.CambioEstadoRequest
import com.example.backend_oportunidades.dto.OportunidadDetalleDto
import com.example.backend_oportunidades.dto.OportunidadItemDto
import com.example.backend_oportunidades.dto.OportunidadRequest
import com.example.backend_oportunidades.service.OportunidadService
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/oportunidades")
class OportunidadController(
    private val oportunidadService: OportunidadService,
    private val usuarioService: UsuarioService
) {

    // Supervisor: todas. Ejecutivo: solo las suyas (lo decide el servicio según el rol)
    @GetMapping
    fun listar(@RequestHeader("X-Usuario-Id") usuarioId: Long): List<OportunidadItemDto> =
        oportunidadService.listar(usuarioService.obtenerActor(usuarioId))

    @GetMapping("/{id}")
    fun obtener(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long
    ): OportunidadDetalleDto =
        oportunidadService.obtener(usuarioService.obtenerActor(usuarioId), id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @Valid @RequestBody solicitud: OportunidadRequest
    ): OportunidadDetalleDto =
        oportunidadService.crear(usuarioService.obtenerActor(usuarioId), solicitud)

    @PutMapping("/{id}")
    fun actualizar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: OportunidadRequest
    ): OportunidadDetalleDto =
        oportunidadService.actualizar(usuarioService.obtenerActor(usuarioId), id, solicitud)

    // PATCH = cambiar solo una parte del recurso (aquí, su estado)
    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: CambioEstadoRequest
    ): OportunidadDetalleDto =
        oportunidadService.cambiarEstado(usuarioService.obtenerActor(usuarioId), id, solicitud)

    @PatchMapping("/{id}/responsable")
    fun asignar(
        @RequestHeader("X-Usuario-Id") usuarioId: Long,
        @PathVariable("id") id: Long,
        @Valid @RequestBody solicitud: AsignarRequest
    ): OportunidadDetalleDto =
        oportunidadService.asignar(usuarioService.obtenerActor(usuarioId), id, solicitud)
}