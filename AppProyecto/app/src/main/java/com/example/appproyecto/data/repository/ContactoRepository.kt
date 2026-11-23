package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aContacto
import com.example.appproyecto.data.remote.aRequest
import com.example.appproyecto.data.remote.llamarApi

/** Contactos de cada organización, consultados al backend. */
class ContactoRepository(private val api: ApiService) {

    suspend fun listarPorOrganizacion(organizacionId: Long): Respuesta<List<Contacto>> =
        llamarApi { api.contactosDeOrganizacion(organizacionId).map { it.aContacto() } }

    suspend fun obtener(id: Long): Respuesta<Contacto> =
        llamarApi { api.contacto(id).aContacto() }

    suspend fun crear(contacto: Contacto): Respuesta<Unit> =
        llamarApi { api.crearContacto(contacto.organizacionId, contacto.aRequest()); Unit }

    suspend fun actualizar(contacto: Contacto): Respuesta<Unit> =
        llamarApi { api.actualizarContacto(contacto.id, contacto.aRequest()); Unit }
}