package com.example.backend_oportunidades.controller

import com.example.backend_oportunidades.dto.LoginRequest
import com.example.backend_oportunidades.dto.UsuarioDto
import com.example.backend_oportunidades.dto.aDto
import com.example.backend_oportunidades.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController(private val usuarioService: UsuarioService) {

    // @Valid hace que Spring revise las anotaciones del DTO (@NotBlank, @Pattern...) antes de entrar
    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody solicitud: LoginRequest): UsuarioDto =
        usuarioService.iniciarSesion(solicitud)

    // Si falta el encabezado X-Usuario-Id, Spring responde 401 (lo maneja ManejadorErrores)
    @GetMapping("/auth/me")
    fun yo(@RequestHeader("X-Usuario-Id") usuarioId: Long): UsuarioDto =
        usuarioService.obtenerActor(usuarioId).aDto()

    @GetMapping("/usuarios/ejecutivos")
    fun ejecutivos(@RequestHeader("X-Usuario-Id") usuarioId: Long): List<UsuarioDto> {
        usuarioService.obtenerActor(usuarioId) // comprueba que quien pregunta existe y está activo
        return usuarioService.listarEjecutivos()
    }
}