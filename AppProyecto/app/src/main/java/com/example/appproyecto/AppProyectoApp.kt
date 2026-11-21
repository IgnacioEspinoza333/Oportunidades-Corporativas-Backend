package com.example.appproyecto

import android.app.Application
import com.example.appproyecto.data.local.sembrarDatosDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppProyectoApp : Application() {

    // Los ViewModels lo piden a través de la fábrica
    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
        CoroutineScope(Dispatchers.IO).launch {
            sembrarDatosDemo(contenedor.baseDatos)
        }
    }
}