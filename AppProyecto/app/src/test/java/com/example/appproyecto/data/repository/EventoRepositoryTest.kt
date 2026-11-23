package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.EventoDto
import com.example.appproyecto.data.remote.EventoRequest
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.FotosEventos
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class EventoRepositoryTest {

    private val api = mockk<ApiService>()
    private val fotos = mockk<FotosEventos>()
    private val repository = EventoRepository(api, fotos)

    private fun dto(id: Long, nombre: String) = EventoDto(
        id = id, nombre = nombre, tipo = "FERIA_EMPRESARIAL", descripcion = "", fechaInicio = 1000L,
        fechaTermino = 2000L, horario = "10:00", direccion = "Calle 1", comuna = "Santiago",
        region = "Metropolitana", organizacionId = 3, organizacionNombre = "Org", publicoEsperado = 100,
        publicoObjetivo = "", condiciones = "", costoReferencial = 0, observaciones = ""
    )

    private val nuevo = Evento(
        nombre = "Feria Demo", fechaInicio = 1000L, fechaTermino = 2000L, organizacionId = 3,
        publicoEsperado = 100, fotoUri = "/fotos/demo.jpg"
    )

    @Test
    fun listar_agregaLaFotoGuardadaEnElTelefono() = runTest {
        coEvery { api.eventos() } returns listOf(dto(1, "Feria"), dto(2, "Expo"))
        every { fotos.obtener(1L) } returns "/fotos/1.jpg"
        every { fotos.obtener(2L) } returns null

        val respuesta = repository.listar()

        val lista = (respuesta as Respuesta.Exito).datos
        assertEquals(listOf("Feria", "Expo"), lista.map { it.nombre })
        assertEquals("/fotos/1.jpg", lista[0].fotoUri)
        assertEquals(null, lista[1].fotoUri)
    }

    @Test
    fun listar_sinConexion_devuelveError() = runTest {
        coEvery { api.eventos() } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.listar())
    }

    @Test
    fun crear_guardaLaFotoConElIdQueAsignoElServidor() = runTest {
        val enviado = slot<EventoRequest>()
        coEvery { api.crearEvento(capture(enviado)) } returns dto(15, "Feria Demo")
        every { fotos.guardar(any(), any()) } just Runs

        val respuesta = repository.crear(nuevo)

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("Feria Demo", enviado.captured.nombre)
        verify(exactly = 1) { fotos.guardar(15L, "/fotos/demo.jpg") }
    }

    @Test
    fun crear_siElBackendRechaza_noGuardaLaFoto() = runTest {
        coEvery { api.crearEvento(any()) } throws errorHttp(409, "Ya existe un evento con ese nombre")

        val respuesta = repository.crear(nuevo)

        assertEquals(Respuesta.Error("Ya existe un evento con ese nombre", 409), respuesta)
        verify(exactly = 0) { fotos.guardar(any(), any()) }
    }
}