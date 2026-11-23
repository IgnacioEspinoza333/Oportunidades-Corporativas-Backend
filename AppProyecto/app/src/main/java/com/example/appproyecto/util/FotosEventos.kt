package com.example.appproyecto.util

import android.content.Context

/**
 * Guarda en el teléfono la ruta de la foto de cada evento (el backend no almacena fotos).
 * La clave es el id del evento y el valor es la ruta del archivo.
 */
class FotosEventos(context: Context) {

    private val prefs = context.getSharedPreferences("fotos_eventos", Context.MODE_PRIVATE)

    fun obtener(eventoId: Long): String? = prefs.getString(eventoId.toString(), null)

    fun guardar(eventoId: Long, ruta: String?) {
        if (ruta != null) prefs.edit().putString(eventoId.toString(), ruta).apply()
    }
}