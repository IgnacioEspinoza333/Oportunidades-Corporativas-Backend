package com.example.appproyecto.ui.seguimiento

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.detallePrueba
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SeguimientoFormViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val contacto = Contacto(id = 4, organizacionId = 3, nombre = "Marta Díaz")
    private val manana = System.currentTimeMillis() + 86_400_000L

    private fun crear(
        detalle: Respuesta<OportunidadDetalle> = Respuesta.Exito(detallePrueba()),
        contactos: Respuesta<List<Contacto>> = Respuesta.Exito(listOf(contacto))
    ): SeguimientoFormViewModel {
        coEvery { entorno.oportunidadRepository.detalle(10L) } returns detalle
        coEvery { entorno.contactoRepository.listarPorOrganizacion(3L) } returns contactos
        return SeguimientoFormViewModel(entorno.aplicacion, SavedStateHandle(mapOf("id" to 10L)))
    }

    // ---------- carga ----------

    @Test
    fun alIniciar_cargaElNombreYLosContactos() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.bloqueado)
        assertEquals("Feria Andina", estado.nombreOportunidad)
        assertEquals(listOf(contacto), estado.contactos)
    }

    @Test
    fun sinPermiso403o404_quedaBloqueado() {
        for (codigo in listOf(403, 404)) {
            val estado = crear(detalle = Respuesta.Error("Prohibido", codigo)).uiState.value

            assertTrue(estado.bloqueado)
            assertEquals("No tienes acceso a esta oportunidad", estado.errorGeneral)
        }
    }

    @Test
    fun errorDeRedAlCargarElDetalle_bloqueaConSuMensaje() {
        val estado = crear(detalle = Respuesta.Error("Sin conexión", null)).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("Sin conexión", estado.errorGeneral)
    }

    @Test
    fun errorAlCargarContactos_bloqueaConSuMensaje() {
        val estado = crear(contactos = Respuesta.Error("Sin conexión", null)).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("Sin conexión", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun bloqueado_guardarNoHaceNada() {
        val vm = crear(detalle = Respuesta.Error("Prohibido", 403))

        vm.guardar()

        coVerify(exactly = 0) { entorno.oportunidadRepository.registrarSeguimiento(any(), any()) }
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")
        vm.fijarFecha(manana)
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals(TipoAccion.LLAMADA, e.tipo)
        assertEquals(4L, e.contactoId)
        assertEquals("Habló con el contacto", e.resultado)
        assertEquals("Enviar propuesta", e.proximaAccion)
        assertEquals(manana, e.fechaProximo)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun quitarFecha_dejaLaFechaEnNulo() {
        val vm = crear()
        vm.fijarFecha(manana)

        vm.quitarFecha()

        assertNull(vm.uiState.value.fechaProximo)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crear()
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.guardar()
        assertNotNull(vm.uiState.value.errorContacto)
        assertNotNull(vm.uiState.value.errorResultado)

        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")

        assertNull(vm.uiState.value.errorContacto)
        assertNull(vm.uiState.value.errorResultado)
    }

    // ---------- validaciones ----------

    @Test
    fun guardar_sinResultado_muestraErrorYNoGuarda() {
        val vm = crear()

        vm.guardar()

        assertEquals("El resultado es obligatorio", vm.uiState.value.errorResultado)
        assertNull(vm.uiState.value.errorContacto)
        coVerify(exactly = 0) { entorno.oportunidadRepository.registrarSeguimiento(any(), any()) }
    }

    @Test
    fun guardar_accionConContactoSinElegirlo_muestraError() {
        val vm = crear()
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.cambiarResultado("Habló con el contacto")

        vm.guardar()

        assertEquals("Selecciona un contacto", vm.uiState.value.errorContacto)
    }

    @Test
    fun guardar_organizacionSinContactos_muestraErrorEspecifico() {
        val vm = crear(contactos = Respuesta.Exito(emptyList()))
        vm.cambiarTipo(TipoAccion.REUNION)
        vm.cambiarResultado("Reunión realizada")

        vm.guardar()

        assertEquals("La organización aún no tiene contactos registrados", vm.uiState.value.errorContacto)
    }

    @Test
    fun guardar_fechaSinProximaAccion_pideLaAccion() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.fijarFecha(manana)

        vm.guardar()

        assertEquals("Indica cuál será la próxima acción", vm.uiState.value.errorProximaAccion)
    }

    @Test
    fun guardar_accionSinFecha_pideLaFecha() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")

        vm.guardar()

        assertEquals("Selecciona la fecha del próximo seguimiento", vm.uiState.value.errorFecha)
    }

    @Test
    fun guardar_fechaPasadaYObservacionesLargas_muestraErrores() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")
        vm.fijarFecha(1000L)
        vm.cambiarObservaciones("a".repeat(301))

        vm.guardar()

        assertEquals("La fecha no puede ser anterior a hoy", vm.uiState.value.errorFecha)
        assertEquals("Las observaciones no puede superar 300 caracteres", vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar ----------

    @Test
    fun guardar_accionSinContacto_ignoraElContactoElegido() {
        val enviado = slot<Seguimiento>()
        coEvery { entorno.oportunidadRepository.registrarSeguimiento(10L, capture(enviado)) } returns
                Respuesta.Exito(Unit)
        val vm = crear()
        vm.elegirContacto(4L) // el tipo por defecto (SEGUIMIENTO) no usa contacto
        vm.cambiarResultado("  Revisé el estado  ")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertNull(enviado.captured.contactoId)
        assertEquals("Revisé el estado", enviado.captured.resultado)
        assertEquals(TipoAccion.SEGUIMIENTO.name, enviado.captured.tipoAccion)
    }

    @Test
    fun guardar_llamadaConContactoYProximaAccion_losEnvia() {
        val enviado = slot<Seguimiento>()
        coEvery { entorno.oportunidadRepository.registrarSeguimiento(10L, capture(enviado)) } returns
                Respuesta.Exito(Unit)
        val vm = crear()
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion(" Enviar propuesta ")
        vm.fijarFecha(manana)

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(4L, enviado.captured.contactoId)
        assertEquals("Enviar propuesta", enviado.captured.proximaAccion)
        assertEquals(manana, enviado.captured.fechaProximoSeguimiento)
    }

    @Test
    fun guardar_rechazadoPorElBackend_muestraSuMensaje() {
        coEvery { entorno.oportunidadRepository.registrarSeguimiento(any(), any()) } returns
                Respuesta.Error("La oportunidad está cerrada", 409)
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")

        vm.guardar()

        assertEquals("La oportunidad está cerrada", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }
}