package com.example.appproyecto.ui.dashboard

import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Feriado
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.itemPrueba
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val navidad = Feriado(fecha = 1000L, nombre = "Navidad")

    private val items = listOf(
        itemPrueba(1, "Feria Andina"),
        itemPrueba(2, "Expo Verde"),
        itemPrueba(3, "Congreso Salud", EstadoOportunidad.OPORTUNIDAD_CERRADA)
    )

    private fun preparar(
        usuario: Usuario = ana,
        feriados: Respuesta<List<Feriado>> = Respuesta.Exito(listOf(navidad))
    ) {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Exito(usuario)
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Exito(items)
        coEvery { entorno.oportunidadRepository.pendientes() } returns Respuesta.Exito(emptyList())
        coEvery { entorno.feriadoRepository.proximos(any(), any()) } returns feriados
    }

    @Test
    fun alIniciar_calculaElResumenYCargaLosFeriados() {
        preparar()

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertFalse(estado.cargando)
        assertEquals(ana, estado.usuario)
        assertEquals(2, estado.resumen.activas) // la cerrada no cuenta
        assertEquals(listOf(navidad), estado.feriados)
        assertFalse(estado.feriadosError)
        assertNull(estado.error)
    }

    @Test
    fun supervisor_tambienCalculaElResumenDelEquipo() {
        preparar(usuario = ana.copy(perfil = Perfil.SUPERVISOR.name))

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertEquals(2, estado.resumen.activas)
    }

    @Test
    fun siFallaElUsuario_muestraElError() {
        preparar()
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Error("Sin conexión", null)

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.cargando)
    }

    @Test
    fun siFallaLaLista_muestraElError() {
        preparar()
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Error("Servidor caído", 500)

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertEquals("Servidor caído", estado.error)
    }

    @Test
    fun siFallanLosPendientes_elDashboardSeMuestraIgual() {
        preparar()
        coEvery { entorno.oportunidadRepository.pendientes() } returns Respuesta.Error("Sin conexión", null)

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertNull(estado.error)
        assertTrue(estado.resumen.pendientes.isEmpty())
        assertEquals(2, estado.resumen.activas)
    }

    @Test
    fun siFallanLosFeriados_loMarcaSinBloquearElDashboard() {
        preparar(feriados = Respuesta.Error("Sin conexión", null))

        val estado = DashboardViewModel(entorno.aplicacion).uiState.value

        assertTrue(estado.feriadosError)
        assertTrue(estado.feriados.isEmpty())
        assertEquals(2, estado.resumen.activas)
    }

    @Test
    fun reintentar_recargaTodo() {
        preparar()
        val vm = DashboardViewModel(entorno.aplicacion)

        vm.reintentar()

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
        coVerify(exactly = 2) { entorno.feriadoRepository.proximos(any(), any()) }
    }

    @Test
    fun cuandoSeGuardaAlgo_elDashboardSeRecargaSolo() {
        preparar()
        DashboardViewModel(entorno.aplicacion)

        entorno.cambios.value = 1

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
    }
}