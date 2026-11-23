package com.example.appproyecto.ui.supervisor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PanelUiState(
    val oportunidades: List<OportunidadItem> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),
    val cargando: Boolean = true,
    /** Oportunidad que se está reasignando (si hay un diálogo abierto). */
    val reasignando: OportunidadItem? = null,
    /** Resultado de la última operación, para mostrarlo en un Snackbar. */
    val resultado: Resultado? = null,
    /** Error al cargar: la pantalla lo muestra con el botón "Reintentar". */
    val error: String? = null
)

class PanelSupervisorViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as AppProyectoApp).contenedor
    private val repository = contenedor.oportunidadRepository
    private val usuarioRepository = contenedor.usuarioRepository

    private val _uiState = MutableStateFlow(PanelUiState())
    val uiState: StateFlow<PanelUiState> = _uiState.asStateFlow()

    init {
        // Carga al abrir y se vuelve a cargar cada vez que se guarda algo (por ejemplo, una reasignación)
        viewModelScope.launch { repository.cambios.collect { cargar() } }
    }

    private suspend fun cargar() {
        val ejecutivos = (usuarioRepository.ejecutivos() as? Respuesta.Exito)?.datos ?: emptyList()
        when (val r = repository.listar()) {
            is Respuesta.Exito -> _uiState.update {
                it.copy(oportunidades = r.datos, ejecutivos = ejecutivos, cargando = false, error = null)
            }
            is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = r.mensaje) }
        }
    }

    /** Vuelve a pedir los datos después de un error de conexión. */
    fun reintentar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            cargar()
        }
    }

    fun abrirReasignar(item: OportunidadItem) { _uiState.update { it.copy(reasignando = item) } }

    fun cerrarReasignar() { _uiState.update { it.copy(reasignando = null) } }

    fun confirmarReasignar(nuevoResponsableId: Long) {
        val item = _uiState.value.reasignando ?: return
        viewModelScope.launch {
            // El backend vuelve a validar que el usuario sea supervisor y que el destino sea un ejecutivo activo.
            val resultado = when (val r = repository.asignar(item.id, nuevoResponsableId)) {
                is Respuesta.Exito -> Resultado.Exito("Asignada a ${r.datos.responsableNombre}")
                is Respuesta.Error -> Resultado.Error(r.mensaje)
            }
            _uiState.update { it.copy(reasignando = null, resultado = resultado) }
        }
    }

    fun consumirResultado() { _uiState.update { it.copy(resultado = null) } }
}