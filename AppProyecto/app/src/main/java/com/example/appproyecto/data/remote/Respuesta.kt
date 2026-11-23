package com.example.appproyecto.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/** Resultado de una consulta al backend: trae los datos, o un mensaje de error para mostrar. */
sealed interface Respuesta<out T> {
    data class Exito<out T>(val datos: T) : Respuesta<T>

    // codigoHttp es el código que respondió el servidor (401, 404...). Es null si no hubo conexión.
    data class Error(val mensaje: String, val codigoHttp: Int? = null) : Respuesta<Nothing>
}

/**
 * Ejecuta una llamada de Retrofit y devuelve Exito o Error, sin lanzar excepciones.
 * Así los repositorios y ViewModels no necesitan try/catch.
 */
suspend fun <T> llamarApi(llamada: suspend () -> T): Respuesta<T> =
    try {
        Respuesta.Exito(llamada())
    } catch (e: CancellationException) {
        throw e // si la corrutina se cancela (por ejemplo, al salir de la pantalla), no es un error
    } catch (e: Exception) {
        Respuesta.Error(mensajeDeError(e), (e as? HttpException)?.code())
    }