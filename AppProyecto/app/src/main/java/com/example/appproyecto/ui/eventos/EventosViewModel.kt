package com.example.appproyecto.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.EventoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventosUiState(
    val eventos: List<Evento> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class EventosViewModel(private val repository: EventoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EventosUiState())
    val uiState: StateFlow<EventosUiState> = _uiState.asStateFlow()

    /** Pide los eventos al backend. La pantalla lo llama cada vez que se vuelve a mostrar. */
    fun cargar() {
        viewModelScope.launch {
            when (val r = repository.listar()) {
                is Respuesta.Exito -> _uiState.update { EventosUiState(eventos = r.datos, cargando = false) }
                is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun reintentar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        cargar()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as AppProyectoApp).contenedor
                EventosViewModel(c.eventoRepository)
            }
        }
    }
}