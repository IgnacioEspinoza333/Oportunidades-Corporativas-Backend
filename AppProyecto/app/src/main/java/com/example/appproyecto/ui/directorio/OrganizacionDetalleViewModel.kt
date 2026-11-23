package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizacionDetalleUiState(
    val cargando: Boolean = true,
    val organizacion: Organizacion? = null,
    val contactos: List<Contacto> = emptyList(),
    val error: String? = null
)

class OrganizacionDetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val organizaciones: OrganizacionRepository,
    private val contactos: ContactoRepository
) : ViewModel() {

    val organizacionId: Long = savedStateHandle["id"] ?: 0L

    private val _uiState = MutableStateFlow(OrganizacionDetalleUiState())
    val uiState: StateFlow<OrganizacionDetalleUiState> = _uiState.asStateFlow()

    /** Pide al backend la organización y sus contactos. */
    fun cargar() {
        viewModelScope.launch {
            val org = organizaciones.obtener(organizacionId)
            val lista = contactos.listarPorOrganizacion(organizacionId)
            when {
                org is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = org.mensaje) }
                lista is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = lista.mensaje) }
                org is Respuesta.Exito && lista is Respuesta.Exito -> _uiState.update {
                    OrganizacionDetalleUiState(cargando = false, organizacion = org.datos, contactos = lista.datos)
                }
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
                OrganizacionDetalleViewModel(createSavedStateHandle(), c.organizacionRepository, c.contactoRepository)
            }
        }
    }
}