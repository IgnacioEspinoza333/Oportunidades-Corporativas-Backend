package com.example.appproyecto.ui.perfil

import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PerfilViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<UsuarioRepository>()
    private val sesion = mockk<SesionManager>(relaxed = true)
    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")

    @Test
    fun alIniciar_cargaElUsuarioDeLaSesion() {
        coEvery { repository.obtenerActual() } returns Respuesta.Exito(ana)

        val vm = PerfilViewModel(repository, sesion)

        assertEquals(ana, vm.usuario.value)
    }

    @Test
    fun siElBackendFalla_elUsuarioQuedaNulo() {
        coEvery { repository.obtenerActual() } returns Respuesta.Error("Sin conexión", null)

        val vm = PerfilViewModel(repository, sesion)

        assertNull(vm.usuario.value)
    }

    @Test
    fun cerrarSesion_borraLaSesionGuardada() {
        coEvery { repository.obtenerActual() } returns Respuesta.Exito(ana)
        val vm = PerfilViewModel(repository, sesion)

        vm.cerrarSesion()

        verify(exactly = 1) { sesion.cerrar() }
    }
}