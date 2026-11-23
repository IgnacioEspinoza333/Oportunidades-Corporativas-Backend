package com.example.appproyecto.data.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Crea el error que lanza Retrofit cuando el backend responde con [codigo] y, opcionalmente, un [mensaje]. */
fun errorHttp(codigo: Int, mensaje: String? = null): HttpException {
    val json = if (mensaje != null) """{"mensaje":"$mensaje"}""" else ""
    return HttpException(Response.error<Any>(codigo, json.toResponseBody("application/json".toMediaType())))
}

const val MENSAJE_SIN_CONEXION =
    "No se pudo conectar con el servidor. Revisa que esté encendido y que estés en la misma red."