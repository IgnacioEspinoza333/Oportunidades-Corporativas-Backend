package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aOrganizacion
import com.example.appproyecto.data.remote.aRequest
import com.example.appproyecto.data.remote.llamarApi

/** Organizaciones: toda la información viene del backend. */
class OrganizacionRepository(private val api: ApiService) {

    suspend fun listar(): Respuesta<List<Organizacion>> =
        llamarApi { api.organizaciones().map { it.aOrganizacion() } }

    suspend fun obtener(id: Long): Respuesta<Organizacion> =
        llamarApi { api.organizacion(id).aOrganizacion() }

    suspend fun crear(organizacion: Organizacion): Respuesta<Unit> =
        llamarApi { api.crearOrganizacion(organizacion.aRequest()); Unit }

    suspend fun actualizar(organizacion: Organizacion): Respuesta<Unit> =
        llamarApi { api.actualizarOrganizacion(organizacion.id, organizacion.aRequest()); Unit }
}