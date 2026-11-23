package com.example.appproyecto.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RespuestaTest {

    // Arma el error que lanzaría Retrofit cuando el backend responde con un código de error
    private fun errorHttp(codigo: Int, cuerpo: String): HttpException {
        val respuesta = Response.error<Any>(
            codigo,
            cuerpo.toResponseBody("application/json".toMediaType())
        )
        return HttpException(respuesta)
    }

    @Test
    fun devuelveExitoConLosDatosCuandoLaLlamadaFunciona() {
        runBlocking {
            val respuesta = llamarApi { "datos" }
            assertEquals(Respuesta.Exito("datos"), respuesta)
        }
    }

    @Test
    fun devuelveErrorConMensajeYCodigoCuandoElBackendRechaza() {
        runBlocking {
            val respuesta = llamarApi<String> {
                throw errorHttp(401, """{"mensaje":"Correo o contraseña incorrectos"}""")
            }
            assertEquals(Respuesta.Error("Correo o contraseña incorrectos", 401), respuesta)
        }
    }

    @Test
    fun devuelveErrorSinCodigoCuandoNoHayConexion() {
        runBlocking {
            val respuesta = llamarApi<String> { throw IOException("sin red") }
            assertTrue(respuesta is Respuesta.Error && respuesta.codigoHttp == null)
        }
    }
}