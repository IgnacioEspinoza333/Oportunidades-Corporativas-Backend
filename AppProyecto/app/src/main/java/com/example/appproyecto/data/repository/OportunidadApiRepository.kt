package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.AsignarRequest
import com.example.appproyecto.data.remote.CambioEstadoRequest
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aRequest
import com.example.appproyecto.data.remote.llamarApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Oportunidades y sus gestiones: los permisos y las transiciones de estado las valida el backend.
 * Supervisor: recibe todas. Ejecutivo: recibe solo las suyas.
 */
class OportunidadApiRepository(private val api: ApiService) {

    // Sube cada vez que se guarda algo con éxito; las pantallas lo observan para recargarse solas.
    private val _cambios = MutableStateFlow(0)
    val cambios: StateFlow<Int> = _cambios.asStateFlow()

    private suspend fun <T> mutar(llamada: suspend () -> T): Respuesta<T> {
        val respuesta = llamarApi(llamada)
        if (respuesta is Respuesta.Exito) _cambios.update { it + 1 }
        return respuesta
    }

    // ---------- Consultas ----------

    suspend fun listar(): Respuesta<List<OportunidadItem>> =
        llamarApi { api.oportunidades() }

    suspend fun detalle(id: Long): Respuesta<OportunidadDetalle> =
        llamarApi { api.oportunidad(id) }

    suspend fun historial(id: Long): Respuesta<List<SeguimientoItem>> =
        llamarApi { api.historial(id) }

    /** Próximas gestiones programadas (el backend filtra según el rol). */
    suspend fun pendientes(): Respuesta<List<SeguimientoPendiente>> =
        llamarApi { api.seguimientosPendientes() }

    // ---------- Crear y editar ----------

    suspend fun crear(oportunidad: Oportunidad): Respuesta<Unit> =
        mutar { api.crearOportunidad(oportunidad.aRequest()); Unit }

    suspend fun actualizar(oportunidad: Oportunidad): Respuesta<Unit> =
        mutar { api.actualizarOportunidad(oportunidad.id, oportunidad.aRequest()); Unit }

    // ---------- Estados y asignación ----------

    suspend fun cambiarEstado(
        id: Long,
        nuevo: EstadoOportunidad,
        observacion: String
    ): Respuesta<OportunidadDetalle> =
        mutar { api.cambiarEstado(id, CambioEstadoRequest(nuevo.name, observacion)) }

    suspend fun asignar(id: Long, responsableId: Long): Respuesta<OportunidadDetalle> =
        mutar { api.asignarResponsable(id, AsignarRequest(responsableId)) }

    // ---------- Gestiones (seguimientos) ----------

    /** El backend pone quién la registra y la fecha, y rechaza si la oportunidad está cerrada. */
    suspend fun registrarSeguimiento(oportunidadId: Long, seguimiento: Seguimiento): Respuesta<Unit> =
        mutar { api.registrarSeguimiento(oportunidadId, seguimiento.aRequest()); Unit }
}