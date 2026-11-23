package com.example.appproyecto.ui.perfil

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

class PerfilViewModel(
    private val repository: UsuarioRepository,
    private val sesion: SesionManager
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    init {
        viewModelScope.launch {
            val respuesta = repository.obtenerActual()
            if (respuesta is Respuesta.Exito) _usuario.value = respuesta.datos
        }
    }

    fun cerrarSesion() = sesion.cerrar()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AppProyectoApp
                PerfilViewModel(
                    repository = app.contenedor.usuarioRepository,
                    sesion = app.contenedor.sesion
                )
            }
        }
    }
}