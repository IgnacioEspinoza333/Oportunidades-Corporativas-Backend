package com.example.backend_oportunidades.service

import com.example.backend_oportunidades.dto.LoginRequest
import com.example.backend_oportunidades.dto.UsuarioDto
import com.example.backend_oportunidades.dto.aDto
import com.example.backend_oportunidades.error.NoAutenticadoException
import com.example.backend_oportunidades.model.Perfil
import com.example.backend_oportunidades.model.Usuario
import com.example.backend_oportunidades.repository.UsuarioRepository
import com.example.backend_oportunidades.util.sha256
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// @Transactional: si algo falla a mitad de una operación, se deshacen todos sus cambios.
@Service
@Transactional
class UsuarioService(private val usuarioRepository: UsuarioRepository) {

    /** Devuelve el usuario si correo y contraseña son correctos; si no, error 401. */
    fun iniciarSesion(solicitud: LoginRequest): UsuarioDto {
        val usuario = usuarioRepository.findByCorreo(solicitud.correo.trim().lowercase())
        if (usuario == null || !usuario.activo || usuario.passwordHash != sha256(solicitud.password)) {
            throw NoAutenticadoException("Correo o contraseña incorrectos")
        }
        return usuario.aDto()
    }

    /** Usuario que hace la petición (viene en el encabezado X-Usuario-Id). Debe existir y estar activo. */
    fun obtenerActor(usuarioId: Long): Usuario {
        val usuario = usuarioRepository.findByIdOrNull(usuarioId)
        if (usuario == null || !usuario.activo) {
            throw NoAutenticadoException("Usuario no identificado o inactivo")
        }
        return usuario
    }

    /** Ejecutivos activos: a ellos puede asignar oportunidades el supervisor. */
    fun listarEjecutivos(): List<UsuarioDto> =
        usuarioRepository.findByPerfilAndActivoTrueOrderByNombre(Perfil.EJECUTIVO).map { it.aDto() }
}