package com.example.appproyecto.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.TipoOportunidad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aOportunidad
import com.example.appproyecto.util.Permisos
import com.example.appproyecto.util.validarFechaLimite
import com.example.appproyecto.util.validarSeleccion
import com.example.appproyecto.util.validarTextoObligatorio
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val observaciones: String = "",
    val eventoId: Long? = null,
    val responsableId: Long? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val potencial: Int = 3,
    val afinidad: Int = 3,
    val fechaLimite: Long? = null,
    val eventos: List<Evento> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),
    val esEdicion: Boolean = false,
    val esSupervisor: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    /** El usuario no tiene permiso para ver/editar esta oportunidad (o no se pudo cargar). */
    val bloqueado: Boolean = false,
    val errorNombre: String? = null,
    val errorEvento: String? = null,
    val errorResponsable: String? = null,
    val errorDescripcion: String? = null,
    val errorObservaciones: String? = null,
    val errorFechaLimite: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
) {
    /** Solo el supervisor elige responsable al crear (el ejecutivo siempre es el responsable). */
    val puedeElegirResponsable: Boolean get() = esSupervisor && !esEdicion

    /** Al crear cualquiera propone prioridad; al editar solo la cambia el supervisor. */
    val puedeEditarPrioridad: Boolean get() = esSupervisor || !esEdicion

    /** Evento elegido: se muestra su resumen y sirve para validar la fecha límite. */
    val eventoSeleccionado: Evento? get() = eventos.firstOrNull { it.id == eventoId }
}

class OportunidadFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val usuarioRepository = contenedor.usuarioRepository
    private val eventoRepository = contenedor.eventoRepository

    private val idEditar: Long = savedStateHandle.get<Long>("id") ?: 0L
    private var original: Oportunidad? = null
    private var actor: Usuario? = null

    private val _uiState = MutableStateFlow(FormUiState(esEdicion = idEditar != 0L))
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1) Quién es el usuario (el backend lo identifica por el encabezado)
            val usuario = when (val r = usuarioRepository.obtenerActual()) {
                is Respuesta.Exito -> r.datos
                is Respuesta.Error -> {
                    bloquear(r.mensaje)
                    return@launch
                }
            }
            if (!usuario.activo) {
                bloquear("No hay una sesión activa")
                return@launch
            }
            actor = usuario
            val esSupervisor = Permisos.esSupervisor(usuario)

            // 2) Eventos para el selector
            val eventos = when (val r = eventoRepository.listar()) {
                is Respuesta.Exito -> r.datos
                is Respuesta.Error -> {
                    bloquear(r.mensaje)
                    return@launch
                }
            }

            // 3) Ejecutivos (solo el supervisor elige responsable)
            val ejecutivos = if (esSupervisor) {
                (usuarioRepository.ejecutivos() as? Respuesta.Exito)?.datos ?: emptyList()
            } else emptyList()
            _uiState.update { it.copy(eventos = eventos, ejecutivos = ejecutivos, esSupervisor = esSupervisor) }

            // 4) Si es edición, se cargan los datos (el backend responde 403/404 si no hay acceso)
            if (idEditar != 0L) {
                when (val r = repository.detalle(idEditar)) {
                    is Respuesta.Exito -> {
                        val op = r.datos.aOportunidad()
                        original = op
                        _uiState.update {
                            it.copy(
                                nombre = op.nombre,
                                descripcion = op.descripcion,
                                observaciones = op.observaciones,
                                eventoId = op.eventoId,
                                prioridad = op.prioridad.aEnum(Prioridad.MEDIA),
                                potencial = op.potencial,
                                afinidad = op.afinidad,
                                fechaLimite = op.fechaLimitePostulacion
                            )
                        }
                    }
                    is Respuesta.Error -> {
                        val sinPermiso = r.codigoHttp == 403 || r.codigoHttp == 404
                        bloquear(if (sinPermiso) "No tienes acceso a esta oportunidad" else r.mensaje)
                        return@launch
                    }
                }
            }
            _uiState.update { it.copy(cargando = false) }
        }
    }

    private fun bloquear(mensaje: String) {
        _uiState.update { it.copy(cargando = false, bloqueado = true, errorGeneral = mensaje) }
    }

    // Cada cambio limpia el error de ese campo: el usuario ve que lo está corrigiendo.
    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun cambiarDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, errorDescripcion = null) }
    fun cambiarObservaciones(v: String) = _uiState.update { it.copy(observaciones = v, errorObservaciones = null) }
    fun elegirEvento(id: Long) =
        _uiState.update { it.copy(eventoId = id, errorEvento = null, errorFechaLimite = null, errorGeneral = null) }
    fun elegirResponsable(id: Long) = _uiState.update { it.copy(responsableId = id, errorResponsable = null, errorGeneral = null) }
    fun elegirPrioridad(p: Prioridad) = _uiState.update { it.copy(prioridad = p) }
    fun elegirPotencial(v: Int) = _uiState.update { it.copy(potencial = v) }
    fun elegirAfinidad(v: Int) = _uiState.update { it.copy(afinidad = v) }
    fun fijarFechaLimite(millis: Long) = _uiState.update { it.copy(fechaLimite = millis, errorFechaLimite = null) }
    fun quitarFechaLimite() = _uiState.update { it.copy(fechaLimite = null, errorFechaLimite = null) }

    fun guardar() {
        val s = _uiState.value
        val usuario = actor ?: return
        if (s.guardando || s.bloqueado) return

        // Primero se validan los campos en el teléfono; el backend vuelve a validar reglas y permisos
        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 5, 80)
        val errEvento = validarSeleccion(s.eventoId, "un evento")
        val errResp = if (s.puedeElegirResponsable) validarSeleccion(s.responsableId, "un responsable") else null
        val errDesc = validarTextoOpcional(s.descripcion, "La descripción")
        val errObs = validarTextoOpcional(s.observaciones, "Las observaciones")
        // Al editar, una fecha límite ya guardada (aunque haya vencido) no se vuelve a exigir.
        val fechaSinCambios = original?.fechaLimitePostulacion == s.fechaLimite
        val errLimite = validarFechaLimite(s.fechaLimite, s.eventoSeleccionado?.fechaInicio, permitirPasado = fechaSinCambios)

        if (listOf(errNombre, errEvento, errResp, errDesc, errObs, errLimite).any { it != null }) {
            // Los valores ingresados se conservan: solo se agregan los mensajes de error.
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorEvento = errEvento, errorResponsable = errResp,
                    errorDescripcion = errDesc, errorObservaciones = errObs, errorFechaLimite = errLimite
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val base = original
            val resultado = if (base != null) {
                repository.actualizar(
                    base.copy(
                        nombre = s.nombre.trim(),
                        descripcion = s.descripcion.trim(),
                        observaciones = s.observaciones.trim(),
                        eventoId = s.eventoId!!,
                        prioridad = s.prioridad.name,
                        potencial = s.potencial,
                        afinidad = s.afinidad,
                        fechaLimitePostulacion = s.fechaLimite
                    )
                )
            } else {
                // El tipo de la oportunidad se toma del evento para no pedir el mismo dato dos veces.
                val tipo = s.eventos.firstOrNull { it.id == s.eventoId }?.tipo ?: TipoOportunidad.OTRA.name
                repository.crear(
                    Oportunidad(
                        nombre = s.nombre.trim(),
                        tipo = tipo,
                        descripcion = s.descripcion.trim(),
                        fuente = "Registro manual",
                        eventoId = s.eventoId!!,
                        responsableId = s.responsableId ?: usuario.id,
                        prioridad = s.prioridad.name,
                        potencial = s.potencial,
                        afinidad = s.afinidad,
                        fechaLimitePostulacion = s.fechaLimite,
                        observaciones = s.observaciones.trim()
                    )
                )
            }
            when (resultado) {
                is Respuesta.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                is Respuesta.Error -> _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}