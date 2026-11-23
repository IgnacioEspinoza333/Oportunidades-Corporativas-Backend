package com.example.appproyecto.data.repository

import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.LoginRequest
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.UsuarioDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CancellationException

class UsuarioRepositoryTest {

    private val api = mockk<ApiService>()
    private val repository = UsuarioRepository(api)

    private val dtoAna = UsuarioDto(
        id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", perfil = "EJECUTIVO",
        area = "Comercial", zona = "Región Metropolitana", activo = true
    )
    private val dtoSofia = dtoAna.copy(id = 2, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl")

    // ---------- autenticar ----------

    @Test
    fun autenticar_credencialesCorrectas_devuelveElUsuarioSinContrasena() = runTest {
        coEvery { api.login(LoginRequest("ana@ejemplo.cl", "Clave1234")) } returns dtoAna

        val respuesta = repository.autenticar("  ANA@Ejemplo.CL ", "Clave1234")

        assertTrue(respuesta is Respuesta.Exito)
        val usuario = (respuesta as Respuesta.Exito).datos
        assertEquals(1L, usuario.id)
        assertEquals("Ana (demo)", usuario.nombre)
        assertEquals("", usuario.passwordHash)
        coVerify(exactly = 1) { api.login(LoginRequest("ana@ejemplo.cl", "Clave1234")) }
    }

    @Test
    fun autenticar_credencialesIncorrectas_devuelveElMensajeDelBackend() = runTest {
        coEvery { api.login(any()) } throws errorHttp(401, "Correo o contraseña incorrectos")

        val respuesta = repository.autenticar("ana@ejemplo.cl", "mala")

        assertEquals(Respuesta.Error("Correo o contraseña incorrectos", 401), respuesta)
    }

    @Test
    fun autenticar_servidorApagado_avisaQueNoHayConexion() = runTest {
        coEvery { api.login(any()) } throws IOException("timeout")

        val respuesta = repository.autenticar("ana@ejemplo.cl", "Clave1234")

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), respuesta)
    }

    @Test
    fun autenticar_errorSinCuerpo_usaUnMensajeGenerico() = runTest {
        coEvery { api.login(any()) } throws errorHttp(500)

        val respuesta = repository.autenticar("ana@ejemplo.cl", "Clave1234")

        assertEquals(Respuesta.Error("El servidor respondió con un error (código 500)", 500), respuesta)
    }

    @Test
    fun autenticar_errorInesperado_usaUnMensajeGenerico() = runTest {
        coEvery { api.login(any()) } throws IllegalStateException("algo raro")

        val respuesta = repository.autenticar("ana@ejemplo.cl", "Clave1234")

        assertEquals(Respuesta.Error("Ocurrió un error inesperado. Intenta de nuevo.", null), respuesta)
    }

    @Test
    fun siSeCancelaLaCorrutina_laCancelacionSePropagaYNoSeConvierteEnError() = runTest {
        coEvery { api.usuarioActual() } throws CancellationException("cancelada")

        try {
            repository.obtenerActual()
            fail("La cancelación debía propagarse")
        } catch (e: CancellationException) {
            assertEquals("cancelada", e.message)
        }
    }

    // ---------- obtenerActual ----------

    @Test
    fun obtenerActual_devuelveElUsuarioDeLaSesion() = runTest {
        coEvery { api.usuarioActual() } returns dtoAna

        val respuesta = repository.obtenerActual()

        assertEquals("ana@ejemplo.cl", (respuesta as Respuesta.Exito).datos.correo)
    }

    @Test
    fun obtenerActual_sesionInvalida_devuelveElCodigo401() = runTest {
        coEvery { api.usuarioActual() } throws errorHttp(401, "Usuario no válido")

        val respuesta = repository.obtenerActual()

        assertEquals(Respuesta.Error("Usuario no válido", 401), respuesta)
    }

    // ---------- ejecutivos ----------

    @Test
    fun ejecutivos_devuelveLaListaConvertida() = runTest {
        coEvery { api.ejecutivos() } returns listOf(dtoAna, dtoSofia)

        val respuesta = repository.ejecutivos()

        val usuarios = (respuesta as Respuesta.Exito).datos
        assertEquals(listOf("Ana (demo)", "Sofía (demo)"), usuarios.map { it.nombre })
    }

    @Test
    fun ejecutivos_sinConexion_devuelveError() = runTest {
        coEvery { api.ejecutivos() } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.ejecutivos())
    }
}