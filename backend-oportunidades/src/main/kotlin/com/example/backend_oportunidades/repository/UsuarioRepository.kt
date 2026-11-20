package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Perfil
import com.example.backend_oportunidades.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    // Para el login
    fun findByCorreo(correo: String): Usuario?

    // Ejecutivos activos, a quienes el supervisor puede asignar oportunidades
    fun findByPerfilAndActivoTrueOrderByNombre(perfil: Perfil): List<Usuario>
}