package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.OrganizacionDto
import com.example.appproyecto.data.remote.OrganizacionRequest
import com.example.appproyecto.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class OrganizacionRepositoryTest {

    private val api = mockk<ApiService>()
    private val repository = OrganizacionRepository(api)

    private fun dto(id: Long, nombre: String) = OrganizacionDto(
        id = id, nombre = nombre, tipo = "EMPRESA", rubro = "Eventos", comuna = "Santiago",
        region = "Metropolitana", sitioWeb = "https://ejemplo.cl", publicoAsociado = "Familias",
        observaciones = "Nota"
    )

    private val nueva = Organizacion(
        nombre = "Feria Andina", tipo = "EMPRESA", rubro = "Eventos", comuna = "Santiago",
        region = "Metropolitana", sitioWeb = "https://ejemplo.cl", publicoAsociado = "Familias",
        observaciones = "Nota"
    )

    @Test
    fun listar_devuelveLasOrganizacionesConvertidas() = runTest {
        coEvery { api.organizaciones() } returns listOf(dto(1, "Feria Andina"), dto(2, "Expo Verde"))

        val respuesta = repository.listar()

        val lista = (respuesta as Respuesta.Exito).datos
        assertEquals(listOf("Feria Andina", "Expo Verde"), lista.map { it.nombre })
        assertEquals("Eventos", lista[0].rubro)
    }

    @Test
    fun listar_sinConexion_devuelveError() = runTest {
        coEvery { api.organizaciones() } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.listar())
    }

    @Test
    fun obtener_devuelveLaOrganizacion() = runTest {
        coEvery { api.organizacion(5L) } returns dto(5, "Feria Andina")

        val respuesta = repository.obtener(5L)

        assertEquals(5L, (respuesta as Respuesta.Exito).datos.id)
    }

    @Test
    fun obtener_inexistente_devuelveElCodigo404() = runTest {
        coEvery { api.organizacion(99L) } throws errorHttp(404, "La organización no existe")

        assertEquals(Respuesta.Error("La organización no existe", 404), repository.obtener(99L))
    }

    @Test
    fun crear_enviaLosDatosAlBackend() = runTest {
        val enviada = slot<OrganizacionRequest>()
        coEvery { api.crearOrganizacion(capture(enviada)) } returns dto(10, "Feria Andina")

        val respuesta = repository.crear(nueva)

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("Feria Andina", enviada.captured.nombre)
        assertEquals("EMPRESA", enviada.captured.tipo)
        assertEquals("https://ejemplo.cl", enviada.captured.sitioWeb)
    }

    @Test
    fun crear_nombreDuplicado_devuelveElErrorDelBackend() = runTest {
        coEvery { api.crearOrganizacion(any()) } throws errorHttp(409, "Ya existe una organización con ese nombre")

        val respuesta = repository.crear(nueva)

        assertEquals(Respuesta.Error("Ya existe una organización con ese nombre", 409), respuesta)
    }

    @Test
    fun actualizar_usaElIdDeLaOrganizacion() = runTest {
        val enviada = slot<OrganizacionRequest>()
        coEvery { api.actualizarOrganizacion(7L, capture(enviada)) } returns dto(7, "Feria Andina")

        val respuesta = repository.actualizar(nueva.copy(id = 7L, nombre = "Feria Andina 2"))

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("Feria Andina 2", enviada.captured.nombre)
        coVerify(exactly = 1) { api.actualizarOrganizacion(7L, any()) }
    }

    @Test
    fun actualizar_sinConexion_devuelveError() = runTest {
        coEvery { api.actualizarOrganizacion(any(), any()) } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.actualizar(nueva.copy(id = 7L)))
    }
}