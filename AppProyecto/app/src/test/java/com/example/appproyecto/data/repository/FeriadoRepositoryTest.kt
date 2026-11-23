package com.example.appproyecto.data.repository

import com.example.appproyecto.data.remote.FeriadoDto
import com.example.appproyecto.data.remote.FeriadosService
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.util.parsearFechaIso
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FeriadoRepositoryTest {

    private val api = mockk<FeriadosService>()
    private val repository = FeriadoRepository(api)

    private val hoy = parsearFechaIso("2026-11-23")!!

    private fun feriado(fecha: String, nombre: String) = FeriadoDto(date = fecha, localName = nombre, name = "$nombre (en)")

    @Test
    fun proximos_conSuficientesEnElAnio_noConsultaElSiguiente() = runTest {
        coEvery { api.feriados(2026) } returns listOf(
            feriado("2026-01-01", "Año Nuevo"),
            feriado("2026-12-08", "Inmaculada Concepción"),
            feriado("2026-12-25", "Navidad"),
            feriado("2026-12-31", "Feriado bancario")
        )

        val respuesta = repository.proximos(maximo = 3, ahora = hoy)

        val nombres = (respuesta as Respuesta.Exito).datos.map { it.nombre }
        assertEquals(listOf("Inmaculada Concepción", "Navidad", "Feriado bancario"), nombres)
        coVerify(exactly = 0) { api.feriados(2027) }
    }

    @Test
    fun proximos_conPocosEnElAnio_consultaElAnioSiguiente() = runTest {
        coEvery { api.feriados(2026) } returns listOf(
            feriado("2026-12-08", "Inmaculada Concepción"),
            feriado("2026-12-25", "Navidad")
        )
        coEvery { api.feriados(2027) } returns listOf(feriado("2027-01-01", "Año Nuevo"))

        val respuesta = repository.proximos(maximo = 3, ahora = hoy)

        val nombres = (respuesta as Respuesta.Exito).datos.map { it.nombre }
        assertEquals(listOf("Inmaculada Concepción", "Navidad", "Año Nuevo"), nombres)
        coVerify(exactly = 1) { api.feriados(2027) }
    }

    @Test
    fun proximos_respetaElMaximoPedido() = runTest {
        coEvery { api.feriados(2026) } returns listOf(
            feriado("2026-12-08", "Inmaculada Concepción"),
            feriado("2026-12-25", "Navidad")
        )

        val respuesta = repository.proximos(maximo = 1, ahora = hoy)

        assertEquals(listOf("Inmaculada Concepción"), (respuesta as Respuesta.Exito).datos.map { it.nombre })
    }

    @Test
    fun proximos_descartaFechasInvalidasYUsaElNombreEnInglesSiFaltaElLocal() = runTest {
        coEvery { api.feriados(2026) } returns listOf(
            FeriadoDto(date = "no-es-fecha", localName = "Roto", name = "Broken"),
            FeriadoDto(date = "2026-12-25", localName = "", name = "Christmas")
        )

        val respuesta = repository.proximos(maximo = 1, ahora = hoy)

        assertEquals(listOf("Christmas"), (respuesta as Respuesta.Exito).datos.map { it.nombre })
    }

    @Test
    fun proximos_sinConexion_devuelveError() = runTest {
        coEvery { api.feriados(any()) } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.proximos(ahora = hoy))
    }
}