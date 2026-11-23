package com.example.appproyecto

import android.app.Application

class AppProyectoApp : Application() {

    /** Los objetos compartidos de la app (repositorios, servicio de red, sesión). */
    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        // Los datos demo ahora viven en el backend (los crea al iniciar), no en la app
        contenedor = ContenedorApp(this)
    }
}