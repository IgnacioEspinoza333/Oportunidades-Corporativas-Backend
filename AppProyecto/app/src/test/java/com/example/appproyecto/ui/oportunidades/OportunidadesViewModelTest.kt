package com.example.appproyecto.ui.oportunidades

import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.itemPrueba
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class OportunidadesViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val sofia = ana.copy(id = 10, nombre = "Sofía (demo)", perfil = Perfil.SUPERVISOR.name)

    private val lista = listOf(
        itemPrueba(1, "Feria Andina", EstadoOportunidad.CONTACTO_PENDIENTE, Prioridad.ALTA),
        itemPrueba(2, "Expo Verde", EstadoOportunidad.EN_EVALUACION, Prioridad.MEDIA),
        itemPrueba(3, "Congreso Salud", EstadoOportunidad.CONTACTO_PENDIENTE, Prioridad.BAJA)
    )

    private fun crear(usuario: Usuario = ana, items: List<OportunidadItem> = lista): OportunidadesViewModel {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Exito(usuario)
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Exito(items)
        return OportunidadesViewModel(entorno.aplicacion)
    }

    @Test
    fun alIniciar_cargaLasOportunidadesYElPerfil() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertEquals(Perfil.EJECUTIVO, estado.perfil)
        assertEquals(3, estado.oportunidades.size)
    }

    @Test
    fun supervisor_tienePerfilSupervisor() {
        assertEquals(Perfil.SUPERVISOR, crear(usuario = sofia).uiState.value.perfil)
    }

    @Test
    fun siFallaElUsuario_elPerfilSigueSiendoEjecutivo() {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Error("Sin conexión", null)
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Exito(lista)

        val estado = OportunidadesViewModel(entorno.aplicacion).uiState.value

        assertEquals(Perfil.EJECUTIVO, estado.perfil)
        assertEquals(3, estado.oportunidades.size)
    }

    @Test
    fun errorAlListar_muestraElMensaje() {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Exito(ana)
        coEvery { entorno.oportunidadRepository.listar() } returns Respuesta.Error("Sin conexión", null)

        val estado = OportunidadesViewModel(entorno.aplicacion).uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.cargando)
    }

    @Test
    fun cambiarBusqueda_filtraPorTexto() {
        val vm = crear()

        vm.cambiarBusqueda("expo")

        assertEquals("expo", vm.uiState.value.busqueda)
        assertEquals(listOf("Expo Verde"), vm.uiState.value.oportunidades.map { it.nombre })
    }

    @Test
    fun elegirEstado_filtraYAlTocarDeNuevoSeDesactiva() {
        val vm = crear()

        vm.elegirEstado(EstadoOportunidad.CONTACTO_PENDIENTE)
        assertEquals(2, vm.uiState.value.oportunidades.size)

        vm.elegirEstado(EstadoOportunidad.CONTACTO_PENDIENTE)
        assertNull(vm.uiState.value.estadoFiltro)
        assertEquals(3, vm.uiState.value.oportunidades.size)
    }

    @Test
    fun elegirPrioridad_filtraYAlTocarDeNuevoSeDesactiva() {
        val vm = crear()

        vm.elegirPrioridad(Prioridad.ALTA)
        assertEquals(listOf("Feria Andina"), vm.uiState.value.oportunidades.map { it.nombre })

        vm.elegirPrioridad(Prioridad.ALTA)
        assertNull(vm.uiState.value.prioridadFiltro)
    }

    @Test
    fun reintentar_recargaLaLista() {
        val vm = crear()

        vm.reintentar()

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
    }

    @Test
    fun cuandoSeGuardaAlgo_laListaSeRecargaSola() {
        crear()

        entorno.cambios.value = 1

        coVerify(exactly = 2) { entorno.oportunidadRepository.listar() }
    }
}