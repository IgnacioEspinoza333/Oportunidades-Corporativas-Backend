package com.example.appproyecto.ui.detalle

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.EntornoApp
import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenciasViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    @get:Rule
    val carpeta = TemporaryFolder()

    private val entorno = EntornoApp()
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")
    private val item = EvidenciaItem(1, "/fotos/1.jpg", 1000L, "Stand", "Ana (demo)")

    private fun TestScope.crear(usuario: Respuesta<Usuario> = Respuesta.Exito(ana)): EvidenciasViewModel {
        coEvery { entorno.usuarioRepository.obtenerActual() } returns usuario
        every { entorno.evidenciaRepository.observarPara(10L) } returns flowOf(listOf(item))
        val vm = EvidenciasViewModel(entorno.aplicacion, SavedStateHandle(mapOf("id" to 10L)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun alIniciar_cargaLasEvidencias() = runTest {
        assertEquals(listOf(item), crear().uiState.value.evidencias)
    }

    @Test
    fun prepararFoto_dejaLaFotoPendienteYLimpiaLaDescripcion() = runTest {
        val vm = crear()
        vm.cambiarDescripcion("Texto viejo")
        val foto = carpeta.newFile("a.jpg")

        vm.prepararFoto(foto.absolutePath)

        assertEquals(foto.absolutePath, vm.uiState.value.fotoPendiente)
        assertEquals("", vm.uiState.value.descripcion)
    }

    @Test
    fun prepararOtraFoto_borraElArchivoDeLaAnterior() = runTest {
        val vm = crear()
        val primera = carpeta.newFile("a.jpg")
        val segunda = carpeta.newFile("b.jpg")

        vm.prepararFoto(primera.absolutePath)
        vm.prepararFoto(segunda.absolutePath)

        assertFalse(primera.exists())
        assertEquals(segunda.absolutePath, vm.uiState.value.fotoPendiente)
    }

    @Test
    fun cancelarFoto_borraElArchivoYReiniciaElBorrador() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        vm.prepararFoto(foto.absolutePath)

        vm.cancelarFoto()

        assertFalse(foto.exists())
        assertNull(vm.uiState.value.fotoPendiente)
    }

    @Test
    fun guardar_sinFoto_noHaceNada() = runTest {
        val vm = crear()

        vm.guardar()

        assertNull(vm.uiState.value.resultado)
        coVerify(exactly = 0) { entorno.evidenciaRepository.agregar(any(), any(), any(), any()) }
    }

    @Test
    fun guardar_sinSesion_muestraError() = runTest {
        val vm = crear(usuario = Respuesta.Error("Sin conexión", null))
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)

        vm.guardar()

        assertEquals(Resultado.Error("No hay una sesión activa"), vm.uiState.value.resultado)
    }

    @Test
    fun guardar_usuarioInactivo_muestraError() = runTest {
        val vm = crear(usuario = Respuesta.Exito(ana.copy(activo = false)))
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)

        vm.guardar()

        assertEquals(Resultado.Error("No hay una sesión activa"), vm.uiState.value.resultado)
    }

    @Test
    fun guardar_descripcionMuyLarga_muestraErrorYConservaLaFoto() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        vm.prepararFoto(foto.absolutePath)
        vm.cambiarDescripcion("a".repeat(101))

        vm.guardar()

        assertEquals("La descripción no puede superar 100 caracteres", vm.uiState.value.errorDescripcion)
        assertEquals(foto.absolutePath, vm.uiState.value.fotoPendiente)
    }

    @Test
    fun cambiarDescripcion_limpiaElError() = runTest {
        val vm = crear()
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)
        vm.cambiarDescripcion("a".repeat(101))
        vm.guardar()
        assertNotNull(vm.uiState.value.errorDescripcion)

        vm.cambiarDescripcion("Stand")

        assertNull(vm.uiState.value.errorDescripcion)
    }

    @Test
    fun guardar_exito_reiniciaElBorradorYAvisa() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        coEvery {
            entorno.evidenciaRepository.agregar(ana, 10L, foto.absolutePath, "Stand principal")
        } returns Resultado.Exito("Evidencia guardada")
        vm.prepararFoto(foto.absolutePath)
        vm.cambiarDescripcion("Stand principal")

        vm.guardar()

        val estado = vm.uiState.value
        assertEquals(Resultado.Exito("Evidencia guardada"), estado.resultado)
        assertNull(estado.fotoPendiente)
        assertFalse(estado.guardando)
    }

    @Test
    fun guardar_conError_conservaLaFotoYMuestraElMensaje() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        coEvery { entorno.evidenciaRepository.agregar(any(), any(), any(), any()) } returns
                Resultado.Error("No tienes acceso a esta oportunidad")
        vm.prepararFoto(foto.absolutePath)

        vm.guardar()

        val estado = vm.uiState.value
        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), estado.resultado)
        assertEquals(foto.absolutePath, estado.fotoPendiente)
        assertFalse(estado.guardando)
    }

    @Test
    fun consumirResultado_limpiaElMensaje() = runTest {
        val vm = crear(usuario = Respuesta.Error("x", null))
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)
        vm.guardar()
        assertNotNull(vm.uiState.value.resultado)

        vm.consumirResultado()

        assertNull(vm.uiState.value.resultado)
    }
}