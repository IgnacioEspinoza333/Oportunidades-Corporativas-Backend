package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.ContactoDto
import com.example.appproyecto.data.remote.ContactoRequest
import com.example.appproyecto.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ContactoRepositoryTest {

    private val api = mockk<ApiService>()
    private val repository = ContactoRepository(api)

    private fun dto(id: Long, nombre: String) = ContactoDto(
        id = id, organizacionId = 3, nombre = nombre, cargo = "Jefa de eventos", area = "EVENTOS",
        correo = "marta@ejemplo.cl", telefono = "912345678", medioPreferente = "Correo",
        estado = "Activo", fechaUltimoContacto = null, observaciones = ""
    )

    private val contacto = Contacto(
        organizacionId = 3, nombre = "Marta Díaz", cargo = "Jefa de eventos", area = "EVENTOS",
        correo = "marta@ejemplo.cl", telefono = "912345678"
    )

    @Test
    fun listarPorOrganizacion_devuelveLosContactosConvertidos() = runTest {
        coEvery { api.contactosDeOrganizacion(3L) } returns listOf(dto(1, "Marta Díaz"), dto(2, "Pedro Rojas"))

        val respuesta = repository.listarPorOrganizacion(3L)

        val lista = (respuesta as Respuesta.Exito).datos
        assertEquals(listOf("Marta Díaz", "Pedro Rojas"), lista.map { it.nombre })
        assertEquals(3L, lista[0].organizacionId)
    }

    @Test
    fun listarPorOrganizacion_sinConexion_devuelveError() = runTest {
        coEvery { api.contactosDeOrganizacion(any()) } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.listarPorOrganizacion(3L))
    }

    @Test
    fun obtener_devuelveElContacto() = runTest {
        coEvery { api.contacto(7L) } returns dto(7, "Marta Díaz")

        val respuesta = repository.obtener(7L)

        assertEquals("Marta Díaz", (respuesta as Respuesta.Exito).datos.nombre)
    }

    @Test
    fun obtener_inexistente_devuelveElCodigo404() = runTest {
        coEvery { api.contacto(99L) } throws errorHttp(404, "El contacto no existe")

        assertEquals(Respuesta.Error("El contacto no existe", 404), repository.obtener(99L))
    }

    @Test
    fun crear_loEnviaALaOrganizacionDelContacto() = runTest {
        val enviado = slot<ContactoRequest>()
        coEvery { api.crearContacto(3L, capture(enviado)) } returns dto(10, "Marta Díaz")

        val respuesta = repository.crear(contacto)

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("Marta Díaz", enviado.captured.nombre)
        assertEquals("marta@ejemplo.cl", enviado.captured.correo)
        coVerify(exactly = 1) { api.crearContacto(3L, any()) }
    }

    @Test
    fun crear_correoDuplicado_devuelveElErrorDelBackend() = runTest {
        coEvery { api.crearContacto(any(), any()) } throws
                errorHttp(409, "Ya existe un contacto con ese correo en esta organización")

        val respuesta = repository.crear(contacto)

        assertEquals(
            Respuesta.Error("Ya existe un contacto con ese correo en esta organización", 409),
            respuesta
        )
    }

    @Test
    fun actualizar_usaElIdDelContacto() = runTest {
        val enviado = slot<ContactoRequest>()
        coEvery { api.actualizarContacto(7L, capture(enviado)) } returns dto(7, "Marta Díaz Soto")

        val respuesta = repository.actualizar(contacto.copy(id = 7L, nombre = "Marta Díaz Soto"))

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("Marta Díaz Soto", enviado.captured.nombre)
    }

    @Test
    fun actualizar_sinConexion_devuelveError() = runTest {
        coEvery { api.actualizarContacto(any(), any()) } throws IOException()

        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.actualizar(contacto.copy(id = 7L)))
    }
}