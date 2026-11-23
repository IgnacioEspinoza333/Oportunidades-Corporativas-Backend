package com.example.appproyecto.ui.eventos

import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.EventoRepository
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventosViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<EventoRepository>()
    private val evento = Evento(id = 1, nombre = "Feria Andina", fechaInicio = 1000L, fechaTermino = 2000L, organizacionId = 3)

    @Test
    fun alCrear_estaCargandoYSinEventos() {
        val estado = EventosViewModel(repository).uiState.value

        assertTrue(estado.cargando)
        assertTrue(estado.eventos.isEmpty())
    }

    @Test
    fun cargar_exito_muestraLosEventos() {
        coEvery { repository.listar() } returns Respuesta.Exito(listOf(evento))
        val vm = EventosViewModel(repository)

        vm.cargar()

        assertEquals(listOf(evento), vm.uiState.value.eventos)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun cargar_error_muestraElMensaje() {
        coEvery { repository.listar() } returns Respuesta.Error("Sin conexión", null)
        val vm = EventosViewModel(repository)

        vm.cargar()

        assertEquals("Sin conexión", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun reintentar_despuesDeUnError_cargaLosEventos() {
        coEvery { repository.listar() } returnsMany
                listOf<Respuesta<List<Evento>>>(Respuesta.Error("Sin conexión", null), Respuesta.Exito(listOf(evento)))
        val vm = EventosViewModel(repository)
        vm.cargar()

        vm.reintentar()

        assertNull(vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.eventos.size)
    }
}