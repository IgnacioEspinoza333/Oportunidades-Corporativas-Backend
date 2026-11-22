package com.example.appproyecto.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErroresRedTest {

    // Arma el error que lanzaría Retrofit cuando el backend responde con un código de error
    private fun errorHttp(codigo: Int, cuerpo: String): HttpException {
        val respuesta = Response.error<Any>(
            codigo,
            cuerpo.toResponseBody("application/json".toMediaType())
        )
        return HttpException(respuesta)
    }

    @Test
    fun usaElMensajeQueEnviaElBackend() {
        val error = errorHttp(404, """{"mensaje":"La oportunidad no existe"}""")
        assertEquals("La oportunidad no existe", mensajeDeError(error))
    }

    @Test
    fun siElCuerpoNoEsJsonMuestraElCodigo() {
        val error = errorHttp(500, "texto que no es json")
        assertEquals("El servidor respondió con un error (código 500)", mensajeDeError(error))
    }

    @Test
    fun sinConexionMuestraUnAvisoAmigable() {
        val mensaje = mensajeDeError(IOException("sin red"))
        assertTrue(mensaje.startsWith("No se pudo conectar con el servidor"))
    }

    @Test
    fun otroErrorMuestraMensajeGenerico() {
        val mensaje = mensajeDeError(IllegalStateException("algo falló"))
        assertEquals("Ocurrió un error inesperado. Intenta de nuevo.", mensaje)
    }
}