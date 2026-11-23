package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.AsignarRequest
import com.example.appproyecto.data.remote.CambioEstadoRequest
import com.example.appproyecto.data.remote.OportunidadRequest
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.SeguimientoRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class OportunidadApiRepositoryTest {

    private val api = mockk<ApiService>()
    private val repository = OportunidadApiRepository(api)

    private val detalle = OportunidadDetalle(
        id = 10, nombre = "Feria Andina", tipo = "EVENTO_CORPORATIVO", descripcion = "", fuente = "",
        fechaDeteccion = 1000L, prioridad = "MEDIA", potencial = 3, afinidad = 3,
        estado = "CONTACTO_PENDIENTE", fechaLimitePostulacion = null, fechaActualizacion = 1000L,
        observaciones = "", responsableId = 1, responsableNombre = "Ana (demo)", eventoId = 1,
        eventoNombre = "Feria 2026", fechaInicio = 2000L, fechaTermino = 3000L, horario = "10:00",
        direccion = "Calle 1", comuna = "Santiago", region = "Metropolitana", publicoEsperado = 100,
        publicoObjetivo = "", condiciones = "", costoReferencial = 0, fotoUri = null,
        organizacionId = 3, organizacionNombre = "Org"
    )
    private val item = OportunidadItem(
        id = 10, nombre = "Feria Andina", estado = "CONTACTO_PENDIENTE", prioridad = "MEDIA", potencial = 3,
        eventoNombre = "Feria 2026", eventoFecha = 2000L, comuna = "Santiago", organizacionNombre = "Org",
        responsableId = 1, responsableNombre = "Ana (demo)"
    )
    private val gestion = SeguimientoItem(
        id = 1, tipoAccion = "LLAMADA", fechaHora = 1000L, resultado = "Habló con el contacto",
        proximaAccion = "", fechaProximoSeguimiento = null, estadoNuevo = null, observaciones = "",
        usuarioNombre = "Ana (demo)", contactoNombre = null
    )
    private val nueva = Oportunidad(nombre = "Feria Andina", eventoId = 1, responsableId = 0)

    // ---------- consultas ----------

    @Test
    fun consultas_devuelvenLosDatosDelBackend() = runTest {
        val pendiente = SeguimientoPendiente(10, "Feria Andina", "CONTACTO_PENDIENTE", 1, "Ana (demo)", "Llamar", 5000L)
        coEvery { api.oportunidades() } returns listOf(item)
        coEvery { api.oportunidad(10L) } returns detalle
        coEvery { api.historial(10L) } returns listOf(gestion)
        coEvery { api.seguimientosPendientes() } returns listOf(pendiente)

        assertEquals(Respuesta.Exito(listOf(item)), repository.listar())
        assertEquals(Respuesta.Exito(detalle), repository.detalle(10L))
        assertEquals(Respuesta.Exito(listOf(gestion)), repository.historial(10L))
        assertEquals(Respuesta.Exito(listOf(pendiente)), repository.pendientes())
    }

    @Test
    fun consultas_conErrorDevuelvenElCodigoHttp() = runTest {
        coEvery { api.oportunidad(10L) } throws errorHttp(403, "No tienes acceso a esta oportunidad")
        coEvery { api.oportunidades() } throws IOException()

        assertEquals(Respuesta.Error("No tienes acceso a esta oportunidad", 403), repository.detalle(10L))
        assertEquals(Respuesta.Error(MENSAJE_SIN_CONEXION, null), repository.listar())
    }

    // ---------- crear y editar ----------

    @Test
    fun crear_sinResponsable_noLoEnviaYAvisaDelCambio() = runTest {
        val enviada = slot<OportunidadRequest>()
        coEvery { api.crearOportunidad(capture(enviada)) } returns detalle
        assertEquals(0, repository.cambios.value)

        val respuesta = repository.crear(nueva)

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals(null, enviada.captured.responsableId)
        assertEquals(1, repository.cambios.value)
    }

    @Test
    fun crear_conResponsable_loEnvia() = runTest {
        val enviada = slot<OportunidadRequest>()
        coEvery { api.crearOportunidad(capture(enviada)) } returns detalle

        repository.crear(nueva.copy(responsableId = 4L))

        assertEquals(4L, enviada.captured.responsableId)
    }

    @Test
    fun crear_conError_noAvisaDeCambios() = runTest {
        coEvery { api.crearOportunidad(any()) } throws errorHttp(400, "El potencial debe estar entre 1 y 5")

        val respuesta = repository.crear(nueva)

        assertEquals(Respuesta.Error("El potencial debe estar entre 1 y 5", 400), respuesta)
        assertEquals(0, repository.cambios.value)
    }

    @Test
    fun actualizar_usaElIdYAvisaDelCambio() = runTest {
        coEvery { api.actualizarOportunidad(8L, any()) } returns detalle

        val respuesta = repository.actualizar(nueva.copy(id = 8L))

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals(1, repository.cambios.value)
    }

    // ---------- estados y asignación ----------

    @Test
    fun cambiarEstado_enviaElNuevoEstadoYAvisaDelCambio() = runTest {
        coEvery {
            api.cambiarEstado(10L, CambioEstadoRequest("CONTACTO_REALIZADO", "Ya hablamos"))
        } returns detalle

        val respuesta = repository.cambiarEstado(10L, EstadoOportunidad.CONTACTO_REALIZADO, "Ya hablamos")

        assertEquals(Respuesta.Exito(detalle), respuesta)
        assertEquals(1, repository.cambios.value)
    }

    @Test
    fun cambiarEstado_transicionInvalida_devuelveElErrorDelBackend() = runTest {
        coEvery { api.cambiarEstado(any(), any()) } throws errorHttp(400, "Transición no permitida")

        val respuesta = repository.cambiarEstado(10L, EstadoOportunidad.OPORTUNIDAD_CERRADA, "")

        assertEquals(Respuesta.Error("Transición no permitida", 400), respuesta)
        assertEquals(0, repository.cambios.value)
    }

    @Test
    fun asignar_enviaElResponsableYAvisaDelCambio() = runTest {
        coEvery { api.asignarResponsable(10L, AsignarRequest(5L)) } returns detalle

        val respuesta = repository.asignar(10L, 5L)

        assertEquals(Respuesta.Exito(detalle), respuesta)
        assertEquals(1, repository.cambios.value)
    }

    // ---------- gestiones ----------

    @Test
    fun registrarSeguimiento_enviaLosDatosYAvisaDelCambio() = runTest {
        val enviada = slot<SeguimientoRequest>()
        coEvery { api.registrarSeguimiento(10L, capture(enviada)) } returns gestion

        val respuesta = repository.registrarSeguimiento(
            10L, Seguimiento(oportunidadId = 10, usuarioId = 1, tipoAccion = "LLAMADA", resultado = "Habló")
        )

        assertEquals(Respuesta.Exito(Unit), respuesta)
        assertEquals("LLAMADA", enviada.captured.tipoAccion)
        assertEquals("Habló", enviada.captured.resultado)
        assertEquals(1, repository.cambios.value)
    }

    @Test
    fun registrarSeguimiento_oportunidadCerrada_devuelveError() = runTest {
        coEvery { api.registrarSeguimiento(any(), any()) } throws errorHttp(409, "La oportunidad está cerrada")

        val respuesta = repository.registrarSeguimiento(
            10L, Seguimiento(oportunidadId = 10, usuarioId = 1, resultado = "Habló")
        )

        assertEquals(Respuesta.Error("La oportunidad está cerrada", 409), respuesta)
        assertEquals(0, repository.cambios.value)
    }
}