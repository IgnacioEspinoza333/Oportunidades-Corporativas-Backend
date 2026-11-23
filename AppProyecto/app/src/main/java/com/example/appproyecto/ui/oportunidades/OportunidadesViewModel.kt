package com.example.appproyecto.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.filtrarOportunidades
import com.example.appproyecto.util.rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OportunidadesUiState(
    val perfil: Perfil = Perfil.EJECUTIVO,
    val busqueda: String = "",
    val estadoFiltro: EstadoOportunidad? = null,
    val prioridadFiltro: Prioridad? = null,
    val oportunidades: List<OportunidadItem> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class OportunidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val usuarioRepository = contenedor.usuarioRepository

    // Lista completa que entrega el backend; los filtros se aplican sobre esta copia
    private var todas: List<OportunidadItem> = emptyList()

    private val _uiState = MutableStateFlow(OportunidadesUiState())
    val uiState: StateFlow<OportunidadesUiState> = _uiState.asStateFlow()

    init {
        // Carga al abrir y se vuelve a cargar cada vez que se guarda algo (crear, editar, estado...)
        viewModelScope.launch { repository.cambios.collect { cargar() } }
    }

    private suspend fun cargar() {
        (usuarioRepository.obtenerActual() as? Respuesta.Exito)?.let { r ->
            _uiState.update { it.copy(perfil = r.datos.rol()) }
        }
        when (val r = repository.listar()) {
            is Respuesta.Exito -> {
                todas = r.datos
                _uiState.update { it.copy(cargando = false, error = null, oportunidades = filtrar(it)) }
            }
            is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = r.mensaje) }
        }
    }

    private fun filtrar(s: OportunidadesUiState): List<OportunidadItem> =
        filtrarOportunidades(todas, s.busqueda, s.estadoFiltro, s.prioridadFiltro)

    fun reintentar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            cargar()
        }
    }

    fun cambiarBusqueda(texto: String) {
        _uiState.update { val nuevo = it.copy(busqueda = texto); nuevo.copy(oportunidades = filtrar(nuevo)) }
    }

    /** Si tocas el filtro ya activo, se desactiva. */
    fun elegirEstado(estado: EstadoOportunidad) {
        _uiState.update {
            val nuevo = it.copy(estadoFiltro = if (it.estadoFiltro == estado) null else estado)
            nuevo.copy(oportunidades = filtrar(nuevo))
        }
    }

    fun elegirPrioridad(prioridad: Prioridad) {
        _uiState.update {
            val nuevo = it.copy(prioridadFiltro = if (it.prioridadFiltro == prioridad) null else prioridad)
            nuevo.copy(oportunidades = filtrar(nuevo))
        }
    }
}