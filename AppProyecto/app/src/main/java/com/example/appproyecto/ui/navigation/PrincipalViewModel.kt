package com.example.appproyecto.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrincipalUiState(
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    /** La sesión guardada ya no corresponde a un usuario activo: hay que volver al login. */
    val sesionInvalida: Boolean = false,
    /** No se pudo cargar el usuario (por ejemplo, sin conexión): se ofrece reintentar. */
    val error: String? = null
)

/** Carga al usuario de la sesión para saber su rol y armar la navegación. */
class PrincipalViewModel(
    private val repository: UsuarioRepository,
    private val sesion: SesionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrincipalUiState())
    val uiState: StateFlow<PrincipalUiState> = _uiState.asStateFlow()

    init {
        cargarUsuario()
    }

    fun reintentar() = cargarUsuario()

    private fun cargarUsuario() {
        viewModelScope.launch {
            _uiState.value = PrincipalUiState(cargando = true)

            // Sin sesión guardada: hay que volver al login
            if (sesion.usuarioId() == null) {
                _uiState.value = PrincipalUiState(cargando = false, sesionInvalida = true)
                return@launch
            }

            when (val respuesta = repository.obtenerActual()) {
                is Respuesta.Exito -> {
                    val usuario = respuesta.datos
                    if (usuario.activo) {
                        _uiState.value = PrincipalUiState(cargando = false, usuario = usuario)
                    } else {
                        sesion.cerrar()
                        _uiState.value = PrincipalUiState(cargando = false, sesionInvalida = true)
                    }
                }
                is Respuesta.Error -> {
                    if (respuesta.codigoHttp == 401) {
                        // El usuario guardado ya no existe o está inactivo: volver al login
                        sesion.cerrar()
                        _uiState.value = PrincipalUiState(cargando = false, sesionInvalida = true)
                    } else {
                        // Sin conexión u otro fallo: NO se cierra la sesión, se ofrece reintentar
                        _uiState.value = PrincipalUiState(cargando = false, error = respuesta.mensaje)
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AppProyectoApp
                PrincipalViewModel(
                    repository = app.contenedor.usuarioRepository,
                    sesion = app.contenedor.sesion
                )
            }
        }
    }
}