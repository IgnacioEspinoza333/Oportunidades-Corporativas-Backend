package com.example.appproyecto.ui.detalle

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.detallePrueba
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetalleOportunidadViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val sofia = ana.copy(id = 10, nombre = "Sofía (demo)", perfil = Perfil.SUPERVISOR.name)

    private fun gestion(id: Long, fechaProximo: Long?) = SeguimientoItem(
        id = id, tipoAccion = "LLAMADA", fechaHora = 1000L, resultado = "Habló", proximaAccion = "",
        fechaProximoSeguimiento = fechaProximo, estadoNuevo = null, observaciones = "",
        usuarioNombre = "Ana (demo)", contactoNombre = null
    )

    private fun crear(
        usuario: Respuesta<Usuario> = Respuesta.Exito(ana),
        detalle: Respuesta<OportunidadDetalle> = Respuesta.Exito(detallePrueba()),
        historial: List<SeguimientoItem> = emptyList()
    ): DetalleOportunidadViewModel {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns usuario
        coEvery { entorno.oportunidadRepository.detalle(10L) } returns detalle
        coEvery { entorno.oportunidadRepository.historial(10L) } returns Respuesta.Exito(historial)
        return DetalleOportunidadViewModel(entorno.aplicacion, SavedStateHandle(mapOf("id" to 10L)))
    }

    // ---------- carga ----------

    @Test
    fun ejecutivo_veSuOportunidadConSusEstadosDestino() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.sinAcceso)
        assertEquals(Perfil.EJECUTIVO, estado.rol)
        assertEquals("Feria Andina", estado.detalle?.nombre)
        assertEquals(listOf(EstadoOportunidad.CONTACTO_REALIZADO), estado.estadosDestino)
        assertTrue(estado.puedeRegistrarGestion)
        assertNull(estado.proximo)
    }

    @Test
    fun supervisor_puedeConfirmarODescartar() {
        val estado = crear(
            usuario = Respuesta.Exito(sofia),
            detalle = Respuesta.Exito(detallePrueba(EstadoOportunidad.EN_EVALUACION))
        ).uiState.value

        assertEquals(Perfil.SUPERVISOR, estado.rol)
        assertTrue(EstadoOportunidad.PARTICIPACION_CONFIRMADA in estado.estadosDestino)
        assertTrue(EstadoOportunidad.OPORTUNIDAD_DESCARTADA in estado.estadosDestino)
    }

    @Test
    fun oportunidadCerrada_noAdmiteGestionesNiCambios() {
        val estado = crear(detalle = Respuesta.Exito(detallePrueba(EstadoOportunidad.OPORTUNIDAD_CERRADA))).uiState.value

        assertFalse(estado.puedeRegistrarGestion)
        assertTrue(estado.estadosDestino.isEmpty())
    }

    @Test
    fun proximo_esLaGestionMasRecienteConFechaProgramada() {
        val estado = crear(historial = listOf(gestion(3, null), gestion(2, 5000L), gestion(1, 4000L))).uiState.value

        assertEquals(3, estado.historial.size)
        assertEquals(2L, estado.proximo?.id)
    }

    @Test
    fun errores401403404_muestranSinAcceso() {
        for (codigo in listOf(401, 403, 404)) {
            val estado = crear(detalle = Respuesta.Error("Sin permiso", codigo)).uiState.value

            assertTrue("código $codigo", estado.sinAcceso)
            assertFalse(estado.cargando)
            assertNull(estado.detalle)
        }
    }

    @Test
    fun errorDeConexion_muestraElMensajeSinMarcarSinAcceso() {
        val estado = crear(detalle = Respuesta.Error("Sin conexión", null)).uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.sinAcceso)
        assertFalse(estado.cargando)
    }

    @Test
    fun siFallaElUsuario_muestraElError() {
        val estado = crear(usuario = Respuesta.Error("Sin conexión", null)).uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.cargando)
    }

    @Test
    fun conLaOportunidadYaCargada_unFalloDeRedSeAvisaComoResultado() {
        val vm = crear()
        coEvery { entorno.oportunidadRepository.detalle(10L) } returns Respuesta.Error("Sin conexión", null)

        entorno.cambios.value = 1

        val estado = vm.uiState.value
        assertEquals(Resultado.Error("Sin conexión"), estado.resultado)
        assertNotNull(estado.detalle)
    }

    @Test
    fun reintentar_despuesDeUnError_cargaLaOportunidad() {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns Respuesta.Exito(ana)
        coEvery { entorno.oportunidadRepository.historial(10L) } returns Respuesta.Exito(emptyList())
        coEvery { entorno.oportunidadRepository.detalle(10L) } returnsMany
                listOf<Respuesta<OportunidadDetalle>>(Respuesta.Error("Sin conexión", null), Respuesta.Exito(detallePrueba()))
        val vm = DetalleOportunidadViewModel(entorno.aplicacion, SavedStateHandle(mapOf("id" to 10L)))
        assertEquals("Sin conexión", vm.uiState.value.error)

        vm.reintentar()

        assertNull(vm.uiState.value.error)
        assertNotNull(vm.uiState.value.detalle)
    }

    // ---------- diálogo y cambio de estado ----------

    @Test
    fun abrirCerrarDialogoYConsumirResultado_actualizanElEstado() {
        val vm = crear()

        vm.abrirDialogoEstado()
        assertTrue(vm.uiState.value.dialogoEstado)
        vm.cerrarDialogoEstado()
        assertFalse(vm.uiState.value.dialogoEstado)

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "a".repeat(301))
        assertNotNull(vm.uiState.value.resultado)
        vm.consumirResultado()
        assertNull(vm.uiState.value.resultado)
    }

    @Test
    fun cambiarEstado_exito_avisaYCierraElDialogo() {
        coEvery {
            entorno.oportunidadRepository.cambiarEstado(10L, EstadoOportunidad.CONTACTO_REALIZADO, "Ya hablamos")
        } returns Respuesta.Exito(detallePrueba(EstadoOportunidad.CONTACTO_REALIZADO))
        val vm = crear()
        vm.abrirDialogoEstado()

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "  Ya hablamos  ")

        assertEquals(Resultado.Exito("Estado actualizado a «Contacto realizado»"), vm.uiState.value.resultado)
        assertFalse(vm.uiState.value.dialogoEstado)
    }

    @Test
    fun cambiarEstado_observacionMuyLarga_muestraErrorYNoLlamaAlBackend() {
        val vm = crear()

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "a".repeat(301))

        assertEquals(Resultado.Error("La observación no puede superar 300 caracteres"), vm.uiState.value.resultado)
        coVerify(exactly = 0) { entorno.oportunidadRepository.cambiarEstado(any(), any(), any()) }
    }

    @Test
    fun cambiarEstado_rechazadoPorElBackend_muestraSuMensaje() {
        coEvery { entorno.oportunidadRepository.cambiarEstado(any(), any(), any()) } returns
                Respuesta.Error("Solo un supervisor puede confirmar o descartar una oportunidad", 403)
        val vm = crear()

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "")

        assertEquals(
            Resultado.Error("Solo un supervisor puede confirmar o descartar una oportunidad"),
            vm.uiState.value.resultado
        )
    }

    @Test
    fun cuandoSeGuardaAlgo_laPantallaSeRecargaSola() {
        crear()

        entorno.cambios.value = 1

        coVerify(exactly = 2) { entorno.oportunidadRepository.detalle(10L) }
    }
}