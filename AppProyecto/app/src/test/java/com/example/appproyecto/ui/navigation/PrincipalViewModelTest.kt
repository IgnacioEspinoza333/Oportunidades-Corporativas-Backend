package com.example.appproyecto.ui.navigation

import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PrincipalViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<UsuarioRepository>()
    private val sesion = mockk<SesionManager>(relaxed = true)
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")

    private fun crear(conSesion: Boolean = true): PrincipalViewModel {
        every { sesion.usuarioId() } returns if (conSesion) 1L else null
        return PrincipalViewModel(repository, sesion)
    }

    @Test
    fun sinSesionGuardada_pideVolverAlLogin() {
        val estado = crear(conSesion = false).uiState.value

        assertTrue(estado.sesionInvalida)
        assertFalse(estado.cargando)
        coVerify(exactly = 0) { repository.obtenerActual() }
    }

    @Test
    fun usuarioActivo_cargaElUsuario() {
        coEvery { repository.obtenerActual() } returns Respuesta.Exito(ana)

        val estado = crear().uiState.value

        assertEquals(ana, estado.usuario)
        assertFalse(estado.cargando)
        assertFalse(estado.sesionInvalida)
    }

    @Test
    fun usuarioInactivo_cierraLaSesion() {
        coEvery { repository.obtenerActual() } returns Respuesta.Exito(ana.copy(activo = false))

        val estado = crear().uiState.value

        assertTrue(estado.sesionInvalida)
        assertNull(estado.usuario)
        verify(exactly = 1) { sesion.cerrar() }
    }

    @Test
    fun error401_cierraLaSesion() {
        coEvery { repository.obtenerActual() } returns Respuesta.Error("Usuario no válido", 401)

        val estado = crear().uiState.value

        assertTrue(estado.sesionInvalida)
        verify(exactly = 1) { sesion.cerrar() }
    }

    @Test
    fun sinConexion_noCierraLaSesionYOfreceReintentar() {
        coEvery { repository.obtenerActual() } returns Respuesta.Error("Sin conexión", null)

        val estado = crear().uiState.value

        assertEquals("Sin conexión", estado.error)
        assertFalse(estado.sesionInvalida)
        verify(exactly = 0) { sesion.cerrar() }
    }

    @Test
    fun reintentar_despuesDeUnFallo_cargaElUsuario() {
        coEvery { repository.obtenerActual() } returnsMany
                listOf<Respuesta<Usuario>>(Respuesta.Error("Sin conexión", null), Respuesta.Exito(ana))
        val vm = crear()
        assertEquals("Sin conexión", vm.uiState.value.error)

        vm.reintentar()

        assertEquals(ana, vm.uiState.value.usuario)
        assertNull(vm.uiState.value.error)
    }
}