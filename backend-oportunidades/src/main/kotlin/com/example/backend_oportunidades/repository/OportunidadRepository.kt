package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Oportunidad
import org.springframework.data.jpa.repository.JpaRepository

interface OportunidadRepository : JpaRepository<Oportunidad, Long> {
    // Supervisor: findAll(). Ejecutivo: solo las suyas.
    fun findByResponsableIdOrderByFechaActualizacionDesc(responsableId: Long): List<Oportunidad>
}