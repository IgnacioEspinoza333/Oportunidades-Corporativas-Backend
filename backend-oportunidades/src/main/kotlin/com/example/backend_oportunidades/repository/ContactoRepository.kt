package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Contacto
import org.springframework.data.jpa.repository.JpaRepository

interface ContactoRepository : JpaRepository<Contacto, Long> {
    fun findByOrganizacionIdOrderByNombre(organizacionId: Long): List<Contacto>

    // Un correo no puede repetirse dentro de la misma organización
    fun existsByOrganizacionIdAndCorreoIgnoreCase(organizacionId: Long, correo: String): Boolean
    fun existsByOrganizacionIdAndCorreoIgnoreCaseAndIdNot(
        organizacionId: Long, correo: String, id: Long
    ): Boolean
}