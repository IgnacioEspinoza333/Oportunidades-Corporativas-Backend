package com.example.backend_oportunidades.repository

import com.example.backend_oportunidades.model.Organizacion
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizacionRepository : JpaRepository<Organizacion, Long> {
    // Para evitar nombres repetidos al crear y al editar (excluyendo la misma organización)
    fun existsByNombreIgnoreCase(nombre: String): Boolean
    fun existsByNombreIgnoreCaseAndIdNot(nombre: String, id: Long): Boolean
}