package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
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

class OrganizacionFormViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<OrganizacionRepository>()

    private fun crear(id: Long? = null): OrganizacionFormViewModel {
        val argumentos = if (id != null) mapOf("id" to id) else emptyMap()
        return OrganizacionFormViewModel(SavedStateHandle(argumentos), repository)
    }

    private fun OrganizacionFormViewModel.llenarValido() {
        cambiarNombre("Feria Andina")
        cambiarRubro("Eventos")
        cambiarRegion("Metropolitana")
        cambiarComuna("Santiago")
    }

    // ---------- carga ----------

    @Test
    fun organizacionNueva_empiezaSinCargar() {
        val estado = crear().uiState.value

        assertFalse(estado.esEdicion)
        assertFalse(estado.cargando)
    }

    @Test
    fun edicion_cargaLosDatosDeLaOrganizacion() {
        coEvery { repository.obtener(5L) } returns Respuesta.Exito(
            Organizacion(id = 5, nombre = "Feria Andina", rubro = "Eventos", comuna = "Santiago", region = "Metropolitana")
        )

        val estado = crear(5L).uiState.value

        assertTrue(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("Feria Andina", estado.nombre)
        assertEquals("Eventos", estado.rubro)
    }

    @Test
    fun edicion_organizacionConError_muestraElMensaje() {
        coEvery { repository.obtener(5L) } returns Respuesta.Error("La organización no existe", 404)

        val estado = crear(5L).uiState.value

        assertEquals("La organización no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarNombre("Feria")
        vm.cambiarTipo("INSTITUCION")
        vm.cambiarRubro("Eventos")
        vm.cambiarRegion("Metropolitana")
        vm.cambiarComuna("Santiago")
        vm.cambiarSitioWeb("www.feria.cl")
        vm.cambiarPublico("Familias")
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals("Feria", e.nombre)
        assertEquals("INSTITUCION", e.tipo)
        assertEquals("www.feria.cl", e.sitioWeb)
        assertEquals("Familias", e.publicoAsociado)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crear()
        vm.guardar()
        assertNotNull(vm.uiState.value.errorNombre)

        vm.cambiarNombre("Feria Andina")
        vm.cambiarRubro("Eventos")
        vm.cambiarRegion("Metropolitana")
        vm.cambiarComuna("Santiago")

        assertNull(vm.uiState.value.errorNombre)
        assertNull(vm.uiState.value.errorRubro)
        assertNull(vm.uiState.value.errorRegion)
        assertNull(vm.uiState.value.errorComuna)
    }

    // ---------- validaciones ----------

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crear()

        vm.guardar()

        assertEquals("El nombre es obligatorio", vm.uiState.value.errorNombre)
        assertNotNull(vm.uiState.value.errorRubro)
        assertNotNull(vm.uiState.value.errorRegion)
        assertNotNull(vm.uiState.value.errorComuna)
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    @Test
    fun guardar_sitioWebPublicoYObservacionesInvalidos_muestraErrores() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarSitioWeb("esto no es una web")
        vm.cambiarPublico("a".repeat(101))
        vm.cambiarObservaciones("a".repeat(301))

        vm.guardar()

        assertEquals("Ingresa un sitio web válido (ej: www.empresa.cl)", vm.uiState.value.errorSitioWeb)
        assertEquals("El público asociado no puede superar 100 caracteres", vm.uiState.value.errorPublico)
        assertEquals("Las observaciones no puede superar 300 caracteres", vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar ----------

    @Test
    fun guardar_datosValidos_creaLaOrganizacion() {
        val enviada = slot<Organizacion>()
        coEvery { repository.crear(capture(enviada)) } returns Respuesta.Exito(Unit)
        val vm = crear()
        vm.llenarValido()
        vm.cambiarNombre("  Feria Andina  ")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals("Feria Andina", enviada.captured.nombre)
        assertEquals(0L, enviada.captured.id)
    }

    @Test
    fun guardar_nombreDuplicado_muestraElErrorDelBackend() {
        coEvery { repository.crear(any()) } returns
                Respuesta.Error("Ya existe una organización con ese nombre", 409)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertEquals("Ya existe una organización con ese nombre", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun edicion_guardar_actualizaLaOrganizacion() {
        coEvery { repository.obtener(5L) } returns Respuesta.Exito(
            Organizacion(id = 5, nombre = "Feria Andina", rubro = "Eventos", comuna = "Santiago", region = "Metropolitana")
        )
        val enviada = slot<Organizacion>()
        coEvery { repository.actualizar(capture(enviada)) } returns Respuesta.Exito(Unit)
        val vm = crear(5L)
        vm.cambiarNombre("Feria Andina 2")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(5L, enviada.captured.id)
        assertEquals("Feria Andina 2", enviada.captured.nombre)
    }
}