package com.example.appproyecto.ui.eventos

import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.EventoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventoFormViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<EventoRepository>()
    private val organizaciones = mockk<OrganizacionRepository>()
    private val organizacion = Organizacion(id = 1, nombre = "Feria Andina")
    private val manana = System.currentTimeMillis() + 86_400_000L

    private fun crear(lista: Respuesta<List<Organizacion>> = Respuesta.Exito(listOf(organizacion))): EventoFormViewModel {
        coEvery { organizaciones.listar() } returns lista
        return EventoFormViewModel(repository, organizaciones)
    }

    private fun EventoFormViewModel.llenarValido() {
        cambiarNombre("Feria Demo")
        elegirOrganizacion(1L)
        fijarFecha(manana)
        cambiarComuna("Santiago")
        cambiarDireccion("Av. Siempre Viva 123")
        cambiarPublico("100")
    }

    // ---------- carga y campos ----------

    @Test
    fun alIniciar_cargaLasOrganizaciones() {
        assertEquals(listOf(organizacion), crear().uiState.value.organizaciones)
    }

    @Test
    fun siFallaLaCargaDeOrganizaciones_muestraElError() {
        val estado = crear(Respuesta.Error("Sin conexión", null)).uiState.value

        assertEquals("Sin conexión", estado.errorGeneral)
        assertTrue(estado.organizaciones.isEmpty())
    }

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarNombre("Feria")
        vm.cambiarTipo("FERIA_EMPRESARIAL")
        vm.cambiarRegion("Valparaíso")
        vm.cambiarComuna("Viña")
        vm.cambiarDireccion("Calle 1")
        vm.elegirOrganizacion(1L)
        vm.fijarFoto("/ruta/foto.jpg")

        val e = vm.uiState.value
        assertEquals("Feria", e.nombre)
        assertEquals("FERIA_EMPRESARIAL", e.tipo)
        assertEquals("Valparaíso", e.region)
        assertEquals("Viña", e.comuna)
        assertEquals("Calle 1", e.direccion)
        assertEquals(1L, e.organizacionId)
        assertEquals("/ruta/foto.jpg", e.fotoRuta)
    }

    @Test
    fun cambiarPublico_dejaSoloDigitosConMaximoSeis() {
        val vm = crear()

        vm.cambiarPublico("12ab3456789")

        assertEquals("123456", vm.uiState.value.publicoEsperado)
    }

    @Test
    fun cambiarCosto_permiteMenosPeroNoLetras() {
        val vm = crear()

        vm.cambiarCosto("-12a3")

        assertEquals("-123", vm.uiState.value.costo)
    }

    @Test
    fun fijarYQuitarFechas_actualizanElEstado() {
        val vm = crear()

        vm.fijarFecha(manana)
        vm.fijarFechaTermino(manana + 1000)
        assertEquals(manana, vm.uiState.value.fecha)
        assertEquals(manana + 1000, vm.uiState.value.fechaTermino)

        vm.quitarFechaTermino()
        assertNull(vm.uiState.value.fechaTermino)
    }

    @Test
    fun cambiarCampo_limpiaSuError() {
        val vm = crear()
        vm.guardar()
        assertNotNull(vm.uiState.value.errorNombre)
        assertNotNull(vm.uiState.value.errorOrganizacion)
        assertNotNull(vm.uiState.value.errorFecha)

        vm.cambiarNombre("Feria Demo")
        vm.elegirOrganizacion(1L)
        vm.fijarFecha(manana)

        assertNull(vm.uiState.value.errorNombre)
        assertNull(vm.uiState.value.errorOrganizacion)
        assertNull(vm.uiState.value.errorFecha)
    }

    // ---------- validaciones ----------

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crear()

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("El nombre es obligatorio", e.errorNombre)
        assertEquals("Selecciona una organización", e.errorOrganizacion)
        assertEquals("Selecciona la fecha del evento", e.errorFecha)
        assertNotNull(e.errorComuna)
        assertNotNull(e.errorDireccion)
        assertEquals("El público esperado debe ser un número", e.errorPublico)
        assertNull(e.errorCosto)
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    @Test
    fun guardar_fechaPasada_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.fijarFecha(1000L)

        vm.guardar()

        assertEquals("La fecha del evento no puede ser anterior a hoy", vm.uiState.value.errorFecha)
    }

    @Test
    fun guardar_terminoAnteriorAlInicio_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.fijarFechaTermino(manana - 1000)

        vm.guardar()

        assertEquals("La fecha de término no puede ser anterior al inicio", vm.uiState.value.errorFechaTermino)
    }

    @Test
    fun guardar_costoNegativo_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarCosto("-5")

        vm.guardar()

        assertEquals("El costo no puede ser negativo", vm.uiState.value.errorCosto)
    }

    // ---------- guardar ----------

    @Test
    fun guardar_datosMinimos_usaValoresPorDefecto() {
        val enviado = slot<Evento>()
        coEvery { repository.crear(capture(enviado)) } returns Respuesta.Exito(Unit)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(0, enviado.captured.costoReferencial)
        assertEquals(manana, enviado.captured.fechaTermino)
        assertEquals("Por definir", enviado.captured.horario)
        assertEquals(100, enviado.captured.publicoEsperado)
        assertNull(enviado.captured.fotoUri)
    }

    @Test
    fun guardar_conTerminoCostoYFoto_losEnvia() {
        val enviado = slot<Evento>()
        coEvery { repository.crear(capture(enviado)) } returns Respuesta.Exito(Unit)
        val vm = crear()
        vm.llenarValido()
        vm.fijarFechaTermino(manana + 86_400_000L)
        vm.cambiarCosto("5000")
        vm.fijarFoto("/ruta/foto.jpg")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(5000, enviado.captured.costoReferencial)
        assertEquals(manana + 86_400_000L, enviado.captured.fechaTermino)
        assertEquals("/ruta/foto.jpg", enviado.captured.fotoUri)
    }

    @Test
    fun guardar_rechazadoPorElBackend_muestraSuMensaje() {
        coEvery { repository.crear(any()) } returns
                Respuesta.Error("Ya existe un evento con ese nombre, fecha y organización", 409)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertEquals("Ya existe un evento con ese nombre, fecha y organización", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }
}