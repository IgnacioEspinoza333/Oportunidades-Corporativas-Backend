package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.ContactoRepository
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

class ContactoFormViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<ContactoRepository>()
    private val organizaciones = mockk<OrganizacionRepository>()
    private val organizacion = Organizacion(id = 3, nombre = "Feria Andina")

    /** [id] distinto de 0 = edición del contacto con ese id. */
    private fun crear(
        org: Respuesta<Organizacion> = Respuesta.Exito(organizacion),
        contacto: Respuesta<Contacto>? = null,
        id: Long = 0L
    ): ContactoFormViewModel {
        coEvery { organizaciones.obtener(3L) } returns org
        if (contacto != null) coEvery { repository.obtener(id) } returns contacto
        val argumentos = if (id != 0L) mapOf("orgId" to 3L, "id" to id) else mapOf("orgId" to 3L)
        return ContactoFormViewModel(SavedStateHandle(argumentos), repository, organizaciones)
    }

    private fun ContactoFormViewModel.llenarValido() {
        cambiarNombre("Marta Díaz")
        cambiarCargo("Jefa de eventos")
        cambiarCorreo("marta@ejemplo.cl")
        cambiarTelefono("912345678")
    }

    // ---------- carga ----------

    @Test
    fun contactoNuevo_cargaElNombreDeLaOrganizacion() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.esEdicion)
        assertEquals("Feria Andina", estado.organizacionNombre)
    }

    @Test
    fun organizacionConError_muestraElMensaje() {
        val estado = crear(org = Respuesta.Error("La organización no existe", 404)).uiState.value

        assertEquals("La organización no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun edicion_cargaLosDatosDelContacto() {
        val existente = Contacto(
            id = 7, organizacionId = 3, nombre = "Marta Díaz", cargo = "Jefa", correo = "marta@ejemplo.cl",
            telefono = "912345678", medioPreferente = "WhatsApp", estado = "Inactivo", observaciones = "Nota"
        )

        val estado = crear(contacto = Respuesta.Exito(existente), id = 7L).uiState.value

        assertTrue(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("Marta Díaz", estado.nombre)
        assertEquals("WhatsApp", estado.medioPreferente)
        assertEquals("Inactivo", estado.estado)
    }

    @Test
    fun edicion_contactoConError_muestraElMensaje() {
        val estado = crear(contacto = Respuesta.Error("El contacto no existe", 404), id = 7L).uiState.value

        assertEquals("El contacto no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarNombre("Marta")
        vm.cambiarCargo("Jefa")
        vm.cambiarArea("MARKETING")
        vm.cambiarCorreo("marta@ejemplo.cl")
        vm.cambiarTelefono("912345678")
        vm.cambiarMedio("WhatsApp")
        vm.cambiarEstado("Inactivo")
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals("Marta", e.nombre)
        assertEquals("Jefa", e.cargo)
        assertEquals("MARKETING", e.area)
        assertEquals("marta@ejemplo.cl", e.correo)
        assertEquals("912345678", e.telefono)
        assertEquals("WhatsApp", e.medioPreferente)
        assertEquals("Inactivo", e.estado)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crear()
        vm.guardar()
        assertNotNull(vm.uiState.value.errorNombre)

        vm.cambiarNombre("Marta")
        vm.cambiarCargo("Jefa")
        vm.cambiarCorreo("marta@ejemplo.cl")

        assertNull(vm.uiState.value.errorNombre)
        assertNull(vm.uiState.value.errorCargo)
        assertNull(vm.uiState.value.errorCorreo)
    }

    // ---------- validaciones ----------

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crear()

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("El nombre es obligatorio", e.errorNombre)
        assertEquals("El cargo es obligatorio", e.errorCargo)
        assertEquals("El correo es obligatorio", e.errorCorreo)
        assertNull(e.errorTelefono)
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    @Test
    fun guardar_telefonoInvalidoYObservacionesLargas_muestraErrores() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarTelefono("123")
        vm.cambiarObservaciones("a".repeat(301))

        vm.guardar()

        assertEquals("El teléfono debe tener entre 9 y 11 dígitos", vm.uiState.value.errorTelefono)
        assertEquals("Las observaciones no puede superar 300 caracteres", vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar ----------

    @Test
    fun guardar_datosValidos_creaElContacto() {
        val enviado = slot<Contacto>()
        coEvery { repository.crear(capture(enviado)) } returns Respuesta.Exito(Unit)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertEquals("Marta Díaz", enviado.captured.nombre)
        assertEquals(3L, enviado.captured.organizacionId)
    }

    @Test
    fun guardar_correoDuplicado_muestraElErrorDelBackend() {
        coEvery { repository.crear(any()) } returns
                Respuesta.Error("Ya existe un contacto con ese correo en esta organización", 409)
        val vm = crear()
        vm.llenarValido()

        vm.guardar()

        assertEquals("Ya existe un contacto con ese correo en esta organización", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
    }

    @Test
    fun edicion_guardar_actualizaElContacto() {
        val existente = Contacto(
            id = 7, organizacionId = 3, nombre = "Marta Díaz", cargo = "Jefa", correo = "marta@ejemplo.cl"
        )
        val enviado = slot<Contacto>()
        coEvery { repository.actualizar(capture(enviado)) } returns Respuesta.Exito(Unit)
        val vm = crear(contacto = Respuesta.Exito(existente), id = 7L)
        vm.cambiarNombre("Marta Díaz Soto")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(7L, enviado.captured.id)
        assertEquals("Marta Díaz Soto", enviado.captured.nombre)
    }
}