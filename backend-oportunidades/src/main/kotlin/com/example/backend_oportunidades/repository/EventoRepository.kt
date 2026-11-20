package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Evento
import org.springframework.data.jpa.repository.JpaRepository

interface EventoRepository : JpaRepository<Evento, Long> {
    fun findAllByOrderByFechaInicio(): List<Evento>

    // No puede haber dos eventos con el mismo nombre, fecha y organización
    fun existsByNombreIgnoreCaseAndFechaInicioAndOrganizacionId(
        nombre: String, fechaInicio: Long, organizacionId: Long
    ): Boolean
}