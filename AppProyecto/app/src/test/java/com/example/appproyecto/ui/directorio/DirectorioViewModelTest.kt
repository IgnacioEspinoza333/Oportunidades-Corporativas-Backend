package com.example.appproyecto.ui.directorio

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

class DirectorioViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val organizaciones = mockk<OrganizacionRepository>()
    private val contactos = mockk<ContactoRepository>()

    private val feria = Organizacion(id = 1, nombre = "Feria Andina", rubro = "Eventos", comuna = "Santiago")
    private val expo = Organizacion(id = 2, nombre = "Expo Verde", rubro = "Agricultura", comuna = "Valdivia")

    private fun crearCargado(): DirectorioViewModel {
        coEvery { organizaciones.listar() } returns Respuesta.Exito(listOf(feria, expo))
        coEvery { contactos.listarPorOrganizacion(1L) } returns Respuesta.Exito(
            listOf(Contacto(id = 1, organizacionId = 1, nombre = "Marta"), Contacto(id = 2, organizacionId = 1, nombre = "Pedro"))
        )
        coEvery { contactos.listarPorOrganizacion(2L) } returns Respuesta.Error("Sin conexión", null)
        val vm = DirectorioViewModel(organizaciones, contactos)
        vm.cargar()
        return vm
    }

    @Test
    fun cargar_cuentaLosContactosDeCadaOrganizacion() {
        val estado = crearCargado().uiState.value

        assertFalse(estado.cargando)
        assertEquals(listOf("Feria Andina", "Expo Verde"), estado.organizaciones.map { it.organizacion.nombre })
        assertEquals(listOf(2, 0), estado.organizaciones.map { it.contactos })
    }

    @Test
    fun cargar_error_muestraElMensaje() {
        coEvery { organizaciones.listar() } returns Respuesta.Error("Sin conexión", null)
        val vm = DirectorioViewModel(organizaciones, contactos)

        vm.cargar()

        assertEquals("Sin conexión", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun buscar_filtraPorNombreRubroOComuna() {
        val vm = crearCargado()

        vm.buscar("valdivia")
        assertEquals(listOf("Expo Verde"), vm.uiState.value.organizaciones.map { it.organizacion.nombre })

        vm.buscar("  EVENTOS ")
        assertEquals(listOf("Feria Andina"), vm.uiState.value.organizaciones.map { it.organizacion.nombre })

        vm.buscar("feria")
        assertEquals(1, vm.uiState.value.organizaciones.size)
    }

    @Test
    fun buscar_textoVacio_muestraTodas() {
        val vm = crearCargado()
        vm.buscar("valdivia")

        vm.buscar("")

        assertEquals(2, vm.uiState.value.organizaciones.size)
        assertEquals("", vm.uiState.value.busqueda)
    }

    @Test
    fun reintentar_despuesDeUnError_cargaLasOrganizaciones() {
        coEvery { organizaciones.listar() } returnsMany listOf<Respuesta<List<Organizacion>>>(
            Respuesta.Error("Sin conexión", null), Respuesta.Exito(listOf(feria))
        )
        coEvery { contactos.listarPorOrganizacion(any()) } returns Respuesta.Exito(emptyList())
        val vm = DirectorioViewModel(organizaciones, contactos)
        vm.cargar()

        vm.reintentar()

        assertNull(vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.organizaciones.size)
    }
}