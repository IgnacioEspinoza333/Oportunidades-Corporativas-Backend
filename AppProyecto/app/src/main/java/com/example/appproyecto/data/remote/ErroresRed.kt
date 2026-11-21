package com.example.appproyecto.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Devuelve el mensaje que se le muestra al usuario cuando una llamada al backend falla. */
fun mensajeDeError(error: Throwable): String = when (error) {

    // El backend respondió con un error (400, 403, 404, 409...): usamos su mensaje
    is HttpException -> {
        val cuerpo = try {
            error.response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        val mensaje = try {
            Gson().fromJson(cuerpo, ErrorApi::class.java)?.mensaje
        } catch (e: Exception) {
            null
        }
        mensaje ?: "El servidor respondió con un error (código ${error.code()})"
    }

    // No hubo respuesta: el servidor está apagado, sin red o tardó demasiado
    is IOException ->
        "No se pudo conectar con el servidor. Revisa que esté encendido y que estés en la misma red."

    else -> "Ocurrió un error inesperado. Intenta de nuevo."
}