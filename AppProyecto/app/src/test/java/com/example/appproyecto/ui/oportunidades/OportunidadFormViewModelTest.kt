package com.example.appproyecto.ui.oportunidades

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.detallePrueba
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OportunidadFormViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val sofia = ana.copy(id = 10, nombre = "Sofía (demo)", perfil = Perfil.SUPERVISOR.name)
    private val inicioEvento = System.currentTimeMillis() + 10 * 86_400_000L
    private val evento = Evento(
        id = 1, nombre = "Feria 2026", tipo = "FERIA_EMPRESARIAL", fechaInicio = inicioEvento,
        fechaTermino = inicioEvento, organizacionId = 3
    )

    private fun crear(
        usuario: Respuesta<Usuario> = Respuesta.Exito(ana),
        eventos: Respuesta<List<Evento>> = Respuesta.Exito(listOf(evento)),
        id: Long? = null,
        detalle: Respuesta<OportunidadDetalle> = Respuesta.Exito(detallePrueba())
    ): OportunidadFormViewModel {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns usuario
        coEvery { entorno.usuarioRepository.ejecutivos() } returns Respuesta.Exito(listOf(ana))
        coEvery { entorno.eventoRepository.listar() } returns eventos
        coEvery { entorno.oportunidadRepository.detalle(10L) } returns detalle
        val argumentos = if (id != null) mapOf("id" to id) else emptyMap()
        return OportunidadFormViewModel(entorno.aplicacion, SavedStateHandle(argumentos))
    }

    private fun OportunidadFormViewModel.llenarValido() {
        cambiarNombre("Feria Andina")
        elegirEvento(1L)
    }

    // ---------- carga ----------

    @Test
    fun ejecutivo_cargaEventosSinListaDeEjecutivos() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.esSupervisor)
        assertEquals(listOf(evento), estado.eventos)
        assertTrue(estado.ejecutivos.isEmpty())
        assertFalse(estado.puedeElegirResponsable)
    }

    @Test
    fun supervisor_cargaLosEjecutivosYPuedeElegirResponsable() {
        val estado = crear(usuario = Respuesta.Exito(sofia)).uiState.value

        assertTrue(estado.esSupervisor)
        assertEquals(listOf(ana), estado.ejecutivos)
        assertTrue(estado.puedeElegirResponsable)
    }

    @Test
    fun siFallaElUsuario_quedaBloqueado() {
        val estado = crear(usuario = Respuesta.Error("Sin conexión", null)).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("Sin conexión", estado.errorGeneral)
    }

    @Test
    fun usuarioInactivo_quedaBloqueado() {
        val estado = crear(usuario = Respuesta.Exito(ana.copy(activo = false))).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("No hay una sesión activa", estado.errorGeneral)
    }

    @Test
    fun siFallanLosEventos_quedaBloqueado() {
        val estado = crear(eventos = Respuesta.Error("Sin conexión", null)).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("Sin conexión", estado.errorGeneral)
    }

    @Test
    fun edicion_cargaLosDatosDeLaOportunidad() {
        val estado = crear(id = 10L).uiState.value

        assertTrue(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("Feria Andina", estado.nombre)
        assertEquals(1L, estado.eventoId)
    }

    @Test
    fun edicion_sinPermiso_quedaBloqueado() {
        val estado = crear(id = 10L, detalle = Respuesta.Error("Prohibido", 403)).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("No tienes acceso a esta oportunidad", estado.errorGeneral)
    }

    @Test
    fun edicion_errorDeRed_bloqueaConSuMensaje() {
        val estado = crear(id = 10L, detalle = Respuesta.Error("Sin conexión", null)).uiState.value

        assertEquals("Sin conexión", estado.errorGeneral)
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear(usuario = Respuesta.Exito(sofia))

        vm.cambiarNombre("Feria")
        vm.cambiarDescripcion("Desc")
        vm.cambiarObservaciones("Nota")
        vm.elegirEvento(1L)
        vm.elegirResponsable(1L)
        vm.elegirPrioridad(Prioridad.ALTA)
        vm.elegirPotencial(5)
        vm.elegirAfinidad(4)
        vm.fijarFechaLimite(inicioEvento - 1000)

        val e = vm.uiState.value
        assertEquals("Feria", e.nombre)
        assertEquals(1L, e.responsableId)
        assertEquals(Prioridad.ALTA, e.prioridad)
        assertEquals(5, e.potencial)
        assertEquals(4, e.afinidad)
        assertEquals("Feria 2026", e.eventoSeleccionado?.nombre)

        vm.quitarFechaLimite()
        assertNull(vm.uiState.value.fechaLimite)
    }

    // ---------- validaciones ----------

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crear()

        vm.guardar()

        assertEquals("El nombre es obligatorio", vm.uiState.value.errorNombre)
        assertEquals("Selecciona un evento", vm.uiState.value.errorEvento)
        coVerify(exactly = 0) { entorno.oportunidadRepository.crear(any()) }
    }

    @Test
    fun guardar_supervisorSinResponsable_muestraError() {
        val vm = crear(usuario = Respuesta.Exito(sofia))
        vm.llenarValido()

        vm.guardar()

        assertEquals("Selecciona un responsable", vm.uiState.value.errorResponsable)
    }

    @Test
    fun guardar_descripcionYFechaLimiteInvalidas_muestraErrores() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarDescripcion("a".repeat(301))
        vm.fijarFechaLimite(1000L)

        vm.guardar()

        assertEquals("La descripción no puede superar 300 caracteres", vm.uiState.value.errorDescripcion)
        assertEquals("La fecha límite no puede ser anterior a hoy", vm.uiState.value.errorFechaLimite)
    }

    @Test
    fun guardar_fechaLimitePosteriorAlEvento_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.fijarFechaLimite(inicioEvento + 86_400_000L)

        vm.guardar()

        assertEquals("La fecha límite no puede ser posterior al evento", vm.uiState.value.errorFechaLimite)
    }

    @Test
    fun guardar_sinUsuarioValido_noHaceNada() {
        val vm = crear(usuario = Respuesta.Error("Sin conexión", null))

        vm.guardar()

        coVerify(exactly = 0) { entorno.oportunidadRepository.crear(any()) }
    }

    // ---------- guardar ----------

    @Test
    fun guardar_ejecutivo_creaLaOportunidadASuNombre() {
        val enviada = slot<Oportunidad>()
        coEvery { entorno.oportunidadRepository.crear(capture(enviada)) } returns Respuesta.Exito(Unit)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(1L, enviada.captured.responsableId)
        assertEquals("FERIA_EMPRESARIAL", enviada.captured.tipo)
        assertEquals("Registro manual", enviada.captured.fuente)
    }

    @Test
    fun guardar_supervisor_asignaAlResponsableElegido() {
        val enviada = slot<Oportunidad>()
        coEvery { entorno.oportunidadRepository.crear(capture(enviada)) } returns Respuesta.Exito(Unit)
        val vm = crear(usuario = Respuesta.Exito(sofia))
        vm.llenarValido()
        vm.elegirResponsable(1L)

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(1L, enviada.captured.responsableId)
    }

    @Test
    fun guardar_rechazadoPorElBackend_muestraSuMensaje() {
        coEvery { entorno.oportunidadRepository.crear(any()) } returns
                Respuesta.Error("El potencial y la afinidad deben estar entre 1 y 5", 400)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertEquals("El potencial y la afinidad deben estar entre 1 y 5", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun edicion_guardar_actualizaLaOportunidadExistente() {
        val enviada = slot<Oportunidad>()
        coEvery { entorno.oportunidadRepository.actualizar(capture(enviada)) } returns Respuesta.Exito(Unit)
        val vm = crear(id = 10L)
        vm.cambiarNombre("Feria Andina 2")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(10L, enviada.captured.id)
        assertEquals("Feria Andina 2", enviada.captured.nombre)
    }
}