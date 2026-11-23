package com.example.appproyecto.ui.directorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizacionItem(val organizacion: Organizacion, val contactos: Int)

data class DirectorioUiState(
    val busqueda: String = "",
    val organizaciones: List<OrganizacionItem> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class DirectorioViewModel(
    private val organizaciones: OrganizacionRepository,
    private val contactos: ContactoRepository
) : ViewModel() {

    // Lista completa traída del backend; la búsqueda filtra sobre esta copia
    private var todas: List<OrganizacionItem> = emptyList()

    private val _uiState = MutableStateFlow(DirectorioUiState())
    val uiState: StateFlow<DirectorioUiState> = _uiState.asStateFlow()

    /** Pide las organizaciones al backend. La pantalla la llama cada vez que se vuelve a mostrar. */
    fun cargar() {
        viewModelScope.launch {
            when (val r = organizaciones.listar()) {
                is Respuesta.Exito -> {
                    todas = r.datos.map { OrganizacionItem(it, contarContactos(it.id)) }
                    _uiState.update {
                        it.copy(cargando = false, error = null, organizaciones = filtrar(todas, it.busqueda))
                    }
                }
                is Respuesta.Error -> _uiState.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun reintentar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        cargar()
    }

    fun buscar(texto: String) {
        _uiState.update { it.copy(busqueda = texto, organizaciones = filtrar(todas, texto)) }
    }

    // El backend no tiene un "total de contactos", así que se piden los de cada organización
    private suspend fun contarContactos(organizacionId: Long): Int =
        (contactos.listarPorOrganizacion(organizacionId) as? Respuesta.Exito)?.datos?.size ?: 0

    private fun filtrar(lista: List<OrganizacionItem>, texto: String): List<OrganizacionItem> {
        val q = texto.trim().lowercase()
        if (q.isEmpty()) return lista
        return lista.filter { item ->
            val o = item.organizacion
            listOf(o.nombre, o.rubro, o.comuna).any { it.lowercase().contains(q) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as AppProyectoApp).contenedor
                DirectorioViewModel(c.organizacionRepository, c.contactoRepository)
            }
        }
    }
}