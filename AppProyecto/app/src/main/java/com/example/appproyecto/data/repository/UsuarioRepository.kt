package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.LoginRequest
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aUsuario
import com.example.appproyecto.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Usuarios y acceso: ahora la verificación de credenciales la hace el backend. */
class UsuarioRepository(private val api: ApiService) {

    /** Inicia sesión. Si la clave es incorrecta, el backend responde con su mensaje (401). */
    suspend fun autenticar(correo: String, password: String): Respuesta<Usuario> =
        llamarApi { api.login(LoginRequest(correo.trim().lowercase(), password)).aUsuario() }

    /** Usuario de la sesión actual: el backend lo identifica por el encabezado X-Usuario-Id. */
    suspend fun obtenerActual(): Respuesta<Usuario> =
        llamarApi { api.usuarioActual().aUsuario() }

    /** Ejecutivos activos, a quienes el supervisor puede asignar oportunidades. */
    suspend fun ejecutivos(): Respuesta<List<Usuario>> =
        llamarApi { api.ejecutivos().map { it.aUsuario() } }

    // ---------- TEMPORAL ----------
    // Los ViewModels que aún no se convierten usan estas dos funciones.
    // Se eliminan cuando todos los módulos usen el backend.

    suspend fun obtenerPorId(id: Long): Usuario? {
        val respuesta = obtenerActual()
        return if (respuesta is Respuesta.Exito && respuesta.datos.id == id) respuesta.datos else null
    }

    fun observarEjecutivos(): Flow<List<Usuario>> = flow {
        val respuesta = ejecutivos()
        emit(if (respuesta is Respuesta.Exito) respuesta.datos else emptyList())
    }
}