package com.example.appproyecto.ui.seguimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.requiereContacto
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.validarFechaProximoSeguimiento
import com.example.appproyecto.util.validarProximaAccion
import com.example.appproyecto.util.validarSeleccion
import com.example.appproyecto.util.validarTextoObligatorio
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeguimientoFormUiState(
    val nombreOportunidad: String = "",
    val tipo: TipoAccion = TipoAccion.SEGUIMIENTO,
    val contactoId: Long? = null,
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximo: Long? = null,
    val observaciones: String = "",
    val contactos: List<Contacto> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val bloqueado: Boolean = false,
    val errorContacto: String? = null,
    val errorResultado: String? = null,
    val errorProximaAccion: String? = null,
    val errorFecha: String? = null,
    val errorObservaciones: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
)

class SeguimientoFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val contactoRepository = contenedor.contactoRepository

    private val oportunidadId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _uiState = MutableStateFlow(SeguimientoFormUiState())
    val uiState: StateFlow<SeguimientoFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // El backend responde 403/404 si la oportunidad no existe o el usuario no tiene acceso
            val detalle = when (val r = repository.detalle(oportunidadId)) {
                is Respuesta.Exito -> r.datos
                is Respuesta.Error -> {
                    val sinPermiso = r.codigoHttp == 403 || r.codigoHttp == 404
                    bloquear(if (sinPermiso) "No tienes acceso a esta oportunidad" else r.mensaje)
                    return@launch
                }
            }
            // Solo se ofrecen los contactos de la organización del evento.
            val contactos = when (val r = contactoRepository.listarPorOrganizacion(detalle.organizacionId)) {
                is Respuesta.Exito -> r.datos
                is Respuesta.Error -> {
                    bloquear(r.mensaje)
                    return@launch
                }
            }
            _uiState.update {
                it.copy(nombreOportunidad = detalle.nombre, contactos = contactos, cargando = false)
            }
        }
    }

    private fun bloquear(mensaje: String) {
        _uiState.update { it.copy(cargando = false, bloqueado = true, errorGeneral = mensaje) }
    }

    // Cada cambio limpia el error de ese campo; los valores ingresados nunca se pierden.
    fun cambiarTipo(v: TipoAccion) = _uiState.update { it.copy(tipo = v, errorContacto = null, errorGeneral = null) }
    fun elegirContacto(id: Long) = _uiState.update { it.copy(contactoId = id, errorContacto = null) }
    fun cambiarResultado(v: String) = _uiState.update { it.copy(resultado = v, errorResultado = null, errorGeneral = null) }
    fun cambiarProximaAccion(v: String) =
        _uiState.update { it.copy(proximaAccion = v, errorProximaAccion = null, errorFecha = null) }
    fun fijarFecha(millis: Long) = _uiState.update { it.copy(fechaProximo = millis, errorFecha = null, errorProximaAccion = null) }
    fun quitarFecha() = _uiState.update { it.copy(fechaProximo = null, errorFecha = null, errorProximaAccion = null) }
    fun cambiarObservaciones(v: String) = _uiState.update { it.copy(observaciones = v, errorObservaciones = null) }

    fun guardar() {
        val s = _uiState.value
        if (s.guardando || s.bloqueado || s.cargando) return

        // Primero se validan los campos en el teléfono; el backend vuelve a validar reglas y permisos
        val errContacto = when {
            !s.tipo.requiereContacto -> null
            s.contactos.isEmpty() -> "La organización aún no tiene contactos registrados"
            else -> validarSeleccion(s.contactoId, "un contacto")
        }
        val errResultado = validarTextoObligatorio(s.resultado, "El resultado", 5, 300)
        val errAccion = validarProximaAccion(s.proximaAccion, s.fechaProximo)
        val errFecha = validarFechaProximoSeguimiento(s.fechaProximo, s.proximaAccion)
        val errObs = validarTextoOpcional(s.observaciones, "Las observaciones")

        if (listOf(errContacto, errResultado, errAccion, errFecha, errObs).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorContacto = errContacto, errorResultado = errResultado,
                    errorProximaAccion = errAccion, errorFecha = errFecha, errorObservaciones = errObs
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val respuesta = repository.registrarSeguimiento(
                oportunidadId,
                Seguimiento(
                    oportunidadId = oportunidadId,
                    usuarioId = 0, // lo asigna el backend según el usuario de la sesión
                    contactoId = s.contactoId.takeIf { s.tipo.requiereContacto },
                    tipoAccion = s.tipo.name,
                    resultado = s.resultado.trim(),
                    proximaAccion = s.proximaAccion.trim(),
                    fechaProximoSeguimiento = s.fechaProximo,
                    observaciones = s.observaciones.trim()
                )
            )
            when (respuesta) {
                is Respuesta.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                is Respuesta.Error -> _uiState.update { it.copy(guardando = false, errorGeneral = respuesta.mensaje) }
            }
        }
    }
}