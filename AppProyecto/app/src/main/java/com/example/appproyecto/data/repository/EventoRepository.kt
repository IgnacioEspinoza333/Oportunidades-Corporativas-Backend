package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aEvento
import com.example.appproyecto.data.remote.aRequest
import com.example.appproyecto.data.remote.llamarApi
import com.example.appproyecto.util.FotosEventos

/** Eventos: los datos vienen del backend y la foto del almacén local. */
class EventoRepository(
    private val api: ApiService,
    private val fotos: FotosEventos
) {

    suspend fun listar(): Respuesta<List<Evento>> =
        llamarApi { api.eventos().map { it.aEvento(fotos.obtener(it.id)) } }

    /** Crea el evento en el backend y, si trae foto, la asocia al id que le asignó el servidor. */
    suspend fun crear(evento: Evento): Respuesta<Unit> =
        llamarApi {
            val creado = api.crearEvento(evento.aRequest())
            fotos.guardar(creado.id, evento.fotoUri)
        }
}