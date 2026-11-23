package com.example.appproyecto.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Feriado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.ResumenDashboard
import com.example.appproyecto.util.calcularDashboard
import com.example.appproyecto.util.rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val usuario: Usuario? = null,
    val resumen: ResumenDashboard = ResumenDashboard(),
    val cargando: Boolean = true,
    val error: String? = null,
    /** Próximos feriados de Chile, desde la API externa. */
    val feriados: List<Feriado> = emptyList(),
    /** La API externa falló: se avisa en esa sección, sin afectar al resto del Dashboard. */
    val feriadosError: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val usuarioRepository = contenedor.usuarioRepository
    private val feriadoRepository = contenedor.feriadoRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Carga al abrir y se vuelve a cargar cada vez que se guarda algo (estado, gestión, asignación...)
        viewModelScope.launch { repository.cambios.collect { cargar() } }
        // Los feriados no dependen de los cambios: se piden una vez al abrir
        viewModelScope.launch { cargarFeriados() }
    }

    private suspend fun cargar() {
        val usuario = when (val r = usuarioRepository.obtenerActual()) {
            is Respuesta.Exito -> r.datos
            is Respuesta.Error -> {
                _uiState.update { it.copy(cargando = false, error = r.mensaje) }
                return
            }
        }
        // El backend decide qué ve cada rol: el supervisor todo, el ejecutivo solo lo suyo.
        val items = when (val r = repository.listar()) {
            is Respuesta.Exito -> r.datos
            is Respuesta.Error -> {
                _uiState.update { it.copy(cargando = false, error = r.mensaje) }
                return
            }
        }
        val pendientes = (repository.pendientes() as? Respuesta.Exito)?.datos ?: emptyList()
        _uiState.update {
            it.copy(
                usuario = usuario,
                resumen = calcularDashboard(usuario.rol(), items, pendientes),
                cargando = false,
                error = null
            )
        }
    }

    private suspend fun cargarFeriados() {
        when (val r = feriadoRepository.proximos()) {
            is Respuesta.Exito -> _uiState.update { it.copy(feriados = r.datos, feriadosError = false) }
            is Respuesta.Error -> _uiState.update { it.copy(feriadosError = true) }
        }
    }

    /** Vuelve a pedir los datos después de un error de conexión. */
    fun reintentar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            cargar()
            cargarFeriados()
        }
    }
}