package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Seguimiento
import org.springframework.data.jpa.repository.JpaRepository

interface SeguimientoRepository : JpaRepository<Seguimiento, Long> {
    // Historial de una oportunidad, del más reciente al más antiguo
    fun findByOportunidadIdOrderByFechaHoraDesc(oportunidadId: Long): List<Seguimiento>
}