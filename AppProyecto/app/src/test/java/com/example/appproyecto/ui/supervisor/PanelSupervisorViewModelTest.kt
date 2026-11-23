package com.example.appproyecto.ui.supervisor

import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.detallePrueba
import com.example.appproyecto.itemPrueba
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PanelSupervisorViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val item = itemPrueba(10, "Feria Andina")

    private fun crear(): PanelSupervisorViewModel {
        coEvery { entorno.usuarioRepository.ejecutivos() } returns Respuesta.Exito(listOf(ana))
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Exito(listOf(item))
        return PanelSupervisorViewModel(entorno.aplicacion)
    }

    @Test
    fun alIniciar_cargaOportunidadesYEjecutivos() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertEquals(listOf(item), estado.oportunidades)
        assertEquals(listOf(ana), estado.ejecutivos)
    }

    @Test
    fun errorAlListar_muestraElMensaje() {
        coEvery { entorno.usuarioRepository.ejecutivos() } returns Respuesta.Error("Sin conexión", null)
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Error("Sin conexión", null)

        val estado = PanelSupervisorViewModel(entorno.aplicacion).uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.cargando)
    }

    @Test
    fun abrirYCerrarReasignar_actualizaLaSeleccion() {
        val vm = crear()

        vm.abrirReasignar(item)
        assertEquals(item, vm.uiState.value.reasignando)

        vm.cerrarReasignar()
        assertNull(vm.uiState.value.reasignando)
    }

    @Test
    fun confirmarReasignar_exito_avisaAQuienQuedoAsignada() {
        coEvery { entorno.oportunidadRepository.asignar(10L, 5L) } returns Respuesta.Exito(detallePrueba())
        val vm = crear()
        vm.abrirReasignar(item)

        vm.confirmarReasignar(5L)

        assertEquals(Resultado.Exito("Asignada a Ana (demo)"), vm.uiState.value.resultado)
        assertNull(vm.uiState.value.reasignando)
    }

    @Test
    fun confirmarReasignar_error_muestraElMensajeDelBackend() {
        coEvery { entorno.oportunidadRepository.asignar(any(), any()) } returns
                Respuesta.Error("Solo se puede asignar a un ejecutivo activo", 400)
        val vm = crear()
        vm.abrirReasignar(item)

        vm.confirmarReasignar(5L)

        assertEquals(Resultado.Error("Solo se puede asignar a un ejecutivo activo"), vm.uiState.value.resultado)
    }

    @Test
    fun confirmarReasignar_sinOportunidadSeleccionada_noHaceNada() {
        val vm = crear()

        vm.confirmarReasignar(5L)

        coVerify(exactly = 0) { entorno.oportunidadRepository.asignar(any(), any()) }
    }

    @Test
    fun consumirResultado_limpiaElMensaje() {
        coEvery { entorno.oportunidadRepository.asignar(any(), any()) } returns Respuesta.Exito(detallePrueba())
        val vm = crear()
        vm.abrirReasignar(item)
        vm.confirmarReasignar(5L)

        vm.consumirResultado()

        assertNull(vm.uiState.value.resultado)
    }

    @Test
    fun reintentar_recargaLosDatos() {
        val vm = crear()

        vm.reintentar()

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
    }

    @Test
    fun cuandoSeGuardaAlgo_elPanelSeRecargaSolo() {
        crear()

        entorno.cambios.value = 1

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
    }
}