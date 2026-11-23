package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class OrganizacionDetalleViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val organizaciones = mockk<OrganizacionRepository>()
    private val contactos = mockk<ContactoRepository>()

    private val feria = Organizacion(id = 3, nombre = "Feria Andina")
    private val marta = Contacto(id = 1, organizacionId = 3, nombre = "Marta Díaz")

    private fun crear(id: Long? = 3L): OrganizacionDetalleViewModel {
        val argumentos = if (id != null) mapOf("id" to id) else emptyMap()
        return OrganizacionDetalleViewModel(SavedStateHandle(argumentos), organizaciones, contactos)
    }

    @Test
    fun idDeLaNavegacion_seLeeDelSavedStateHandle() {
        assertEquals(3L, crear(3L).organizacionId)
        assertEquals(0L, crear(null).organizacionId)
    }

    @Test
    fun cargar_exito_muestraLaOrganizacionYSusContactos() {
        coEvery { organizaciones.obtener(3L) } returns Respuesta.Exito(feria)
        coEvery { contactos.listarPorOrganizacion(3L) } returns Respuesta.Exito(listOf(marta))
        val vm = crear()

        vm.cargar()

        val e = vm.uiState.value
        assertFalse(e.cargando)
        assertEquals(feria, e.organizacion)
        assertEquals(listOf(marta), e.contactos)
    }

    @Test
    fun cargar_organizacionConError_muestraElMensaje() {
        coEvery { organizaciones.obtener(3L) } returns Respuesta.Error("La organización no existe", 404)
        coEvery { contactos.listarPorOrganizacion(3L) } returns Respuesta.Exito(emptyList())
        val vm = crear()

        vm.cargar()

        assertEquals("La organización no existe", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun cargar_contactosConError_muestraElMensaje() {
        coEvery { organizaciones.obtener(3L) } returns Respuesta.Exito(feria)
        coEvery { contactos.listarPorOrganizacion(3L) } returns Respuesta.Error("Sin conexión", null)
        val vm = crear()

        vm.cargar()

        assertEquals("Sin conexión", vm.uiState.value.error)
    }

    @Test
    fun reintentar_despuesDeUnError_cargaLosDatos() {
        coEvery { organizaciones.obtener(3L) } returnsMany
                listOf<Respuesta<Organizacion>>(Respuesta.Error("Sin conexión", null), Respuesta.Exito(feria))
        coEvery { contactos.listarPorOrganizacion(3L) } returns Respuesta.Exito(listOf(marta))
        val vm = crear()
        vm.cargar()

        vm.reintentar()

        assertNull(vm.uiState.value.error)
        assertEquals(feria, vm.uiState.value.organizacion)
    }
}