package com.example.appproyecto

import android.app.Application
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.remote.ApiClient
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager

/**
 * Contenedor de dependencias: crea UNA sola vez los objetos que comparten las pantallas
 * y se los entrega a los ViewModels. Así ningún ViewModel arma sus propias herramientas.
 */
class ContenedorApp(application: Application) {

    // Recuerda qué usuario inició sesión
    val sesion = SesionManager(application)

    // Base de datos local (Room): se sigue usando para fotos y datos locales
    val baseDatos: AppDatabase by lazy { AppDatabase.getInstance(application) }

    // Servicio de Retrofit para hablar con el backend
    val api: ApiService by lazy { ApiClient.crearServicio(sesion) }

    // ---------- Repositorios ----------
    // Por ahora el de usuarios sigue usando Room; en el paso 9 pasará a usar el backend.
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepository(baseDatos.usuarioDao()) }
}