package com.example.appproyecto.ui.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.Permisos
import com.example.appproyecto.util.proximoSeguimiento
import com.example.appproyecto.util.rol
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val cargando: Boolean = true,
    /** La oportunidad no existe o el usuario no tiene permiso para verla. */
    val sinAcceso: Boolean = false,
    val rol: Perfil = Perfil.EJECUTIVO,
    val detalle: OportunidadDetalle? = null,
    val historial: List<SeguimientoItem> = emptyList(),
    val proximo: SeguimientoItem? = null,
    /** Estados a los que este usuario puede mover la oportunidad desde el estado actual. */
    val estadosDestino: List<EstadoOportunidad> = emptyList(),
    val puedeRegistrarGestion: Boolean = false,
    val dialogoEstado: Boolean = false,
    val resultado: Resultado? = null,
    /** Error de conexión al cargar (el estado "sin acceso" es aparte). */
    val error: String? = null
)

class DetalleOportunidadViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val usuarioRepository = contenedor.usuarioRepository

    private val oportunidadId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        // Carga al abrir y se vuelve a cargar cada vez que se guarda algo (estado, gestión, edición...)
        viewModelScope.launch { repository.cambios.collect { cargar() } }
    }

    private suspend fun cargar() {
        val rol = when (val r = usuarioRepository.obtenerActual()) {
            is Respuesta.Exito -> r.datos.rol()
            is Respuesta.Error -> {
                mostrarErrorDeConexion(r.mensaje)
                return
            }
        }

        when (val r = repository.detalle(oportunidadId)) {
            is Respuesta.Exito -> {
                val detalle = r.datos
                val historial = (repository.historial(oportunidadId) as? Respuesta.Exito)?.datos ?: emptyList()
                val estado = detalle.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        sinAcceso = false,
                        error = null,
                        rol = rol,
                        detalle = detalle,
                        historial = historial,
                        proximo = proximoSeguimiento(historial),
                        estadosDestino = Permisos.estadosDestino(rol, estado),
                        puedeRegistrarGestion = !Permisos.esFinal(estado)
                    )
                }
            }
            is Respuesta.Error -> {
                // 401, 403 y 404: el backend dice que no puede verla; otro error es de conexión
                if (r.codigoHttp == 401 || r.codigoHttp == 403 || r.codigoHttp == 404) {
                    _uiState.update { it.copy(cargando = false, sinAcceso = true, detalle = null, rol = rol) }
                } else {
                    mostrarErrorDeConexion(r.mensaje)
                }
            }
        }
    }

    private fun mostrarErrorDeConexion(mensaje: String) {
        _uiState.update {
            it.copy(
                cargando = false,
                error = mensaje,
                // Si ya había datos en pantalla, el error se avisa con un Snackbar; si no, la pantalla muestra "Reintentar"
                resultado = if (it.detalle != null) Resultado.Error(mensaje) else it.resultado
            )
        }
    }

    /** Vuelve a pedir los datos después de un error de conexión. */
    fun reintentar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            cargar()
        }
    }

    fun abrirDialogoEstado() { _uiState.update { it.copy(dialogoEstado = true) } }
    fun cerrarDialogoEstado() { _uiState.update { it.copy(dialogoEstado = false) } }
    fun consumirResultado() { _uiState.update { it.copy(resultado = null) } }

    fun cambiarEstado(nuevo: EstadoOportunidad, observacion: String) {
        val error = validarTextoOpcional(observacion, "La observación")
        if (error != null) {
            _uiState.update { it.copy(resultado = Resultado.Error(error)) }
            return
        }
        viewModelScope.launch {
            // El backend valida permisos y transiciones, y deja constancia en el historial.
            val resultado = when (val r = repository.cambiarEstado(oportunidadId, nuevo, observacion.trim())) {
                is Respuesta.Exito -> Resultado.Exito("Estado actualizado a «${nuevo.etiqueta}»")
                is Respuesta.Error -> Resultado.Error(r.mensaje)
            }
            _uiState.update { it.copy(dialogoEstado = false, resultado = resultado) }
        }
    }
}