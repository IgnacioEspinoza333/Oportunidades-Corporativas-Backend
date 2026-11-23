package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EvidenciaDao
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EvidenciaRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val dao = mockk<EvidenciaDao>()
    private val oportunidades = mockk<OportunidadApiRepository>()
    private val repository = EvidenciaRepository(dao, oportunidades)

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
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
    private val item = EvidenciaItem(1, "/fotos/1.jpg", 1000L, "Stand", "Ana (demo)")

    // ---------- observarPara ----------

    @Test
    fun observarPara_conAcceso_devuelveLasEvidenciasDeRoom() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Exito(detalle)
        every { dao.observarItemsPorOportunidad(10L) } returns flowOf(listOf(item))

        assertEquals(listOf(item), repository.observarPara(10L).first())
    }

    @Test
    fun observarPara_sinAcceso_devuelveListaVaciaYNoConsultaRoom() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Error("Prohibido", 403)

        assertEquals(emptyList<EvidenciaItem>(), repository.observarPara(10L).first())
        verify(exactly = 0) { dao.observarItemsPorOportunidad(any()) }
    }

    // ---------- agregar ----------

    @Test
    fun agregar_datosValidos_guardaLaEvidenciaConElNombreDelUsuario() = runTest {
        val guardada = slot<Evidencia>()
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Exito(detalle)
        coEvery { dao.insertar(capture(guardada)) } returns 1L
        val foto = carpeta.newFile("a.jpg")

        val resultado = repository.agregar(ana, 10L, foto.absolutePath, "  Stand principal  ")

        assertEquals(Resultado.Exito("Evidencia guardada"), resultado)
        assertEquals("Stand principal", guardada.captured.descripcion)
        assertEquals("Ana (demo)", guardada.captured.usuarioNombre)
        assertEquals(10L, guardada.captured.oportunidadId)
        assertEquals(foto.absolutePath, guardada.captured.rutaFoto)
    }

    @Test
    fun agregar_sinPermiso403o404_avisaQueNoTieneAcceso() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Error("Prohibido", 403)
        coEvery { oportunidades.detalle(11L) } returns Respuesta.Error("No existe", 404)

        val r403 = repository.agregar(ana, 10L, "/x.jpg", "")
        val r404 = repository.agregar(ana, 11L, "/x.jpg", "")

        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), r403)
        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), r404)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun agregar_otroErrorDeRed_muestraSuMensaje() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Error(MENSAJE_SIN_CONEXION, null)

        assertEquals(Resultado.Error(MENSAJE_SIN_CONEXION), repository.agregar(ana, 10L, "/x.jpg", ""))
    }

    @Test
    fun agregar_descripcionMuyLarga_muestraError() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Exito(detalle)

        val resultado = repository.agregar(ana, 10L, "/x.jpg", "a".repeat(101))

        assertEquals(Resultado.Error("La descripción no puede superar 100 caracteres"), resultado)
    }

    @Test
    fun agregar_archivoInexistente_muestraError() = runTest {
        coEvery { oportunidades.detalle(10L) } returns Respuesta.Exito(detalle)

        val resultado = repository.agregar(ana, 10L, "/ruta/que/no/existe.jpg", "Stand")

        assertEquals(Resultado.Error("No se encontró la fotografía, intenta tomarla de nuevo"), resultado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }
}