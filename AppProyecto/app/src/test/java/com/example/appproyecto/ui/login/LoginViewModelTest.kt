package com.example.appproyecto.ui.login

import com.example.appproyecto.ReglaCorrutinas
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val repository = mockk<UsuarioRepository>()
    private val sesion = mockk<SesionManager>(relaxed = true)
    private val vm = LoginViewModel(repository, sesion)

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "")

    private fun llenarValido() {
        vm.cambiarCorreo("ana@ejemplo.cl")
        vm.cambiarPassword("Clave1234")
    }

    @Test
    fun cambiarCampos_actualizaElEstadoYLimpiaErrores() {
        vm.iniciarSesion()
        assertNotNull(vm.uiState.value.errorCorreo)

        vm.cambiarCorreo("ana@ejemplo.cl")
        vm.cambiarPassword("Clave1234")

        val e = vm.uiState.value
        assertEquals("ana@ejemplo.cl", e.correo)
        assertEquals("Clave1234", e.password)
        assertNull(e.errorCorreo)
        assertNull(e.errorPassword)
    }

    @Test
    fun iniciarSesion_camposVacios_muestraErroresYNoLlamaAlBackend() {
        vm.iniciarSesion()

        assertEquals("El correo es obligatorio", vm.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", vm.uiState.value.errorPassword)
        coVerify(exactly = 0) { repository.autenticar(any(), any()) }
    }

    @Test
    fun iniciarSesion_correoSinArrobaYClaveCorta_muestraErrores() {
        vm.cambiarCorreo("ana.ejemplo.cl")
        vm.cambiarPassword("123")

        vm.iniciarSesion()

        assertEquals("El correo debe contener @", vm.uiState.value.errorCorreo)
        assertEquals("Mínimo 8 caracteres", vm.uiState.value.errorPassword)
    }

    @Test
    fun iniciarSesion_credencialesCorrectas_guardaLaSesion() {
        coEvery { repository.autenticar("ana@ejemplo.cl", "Clave1234") } returns Respuesta.Exito(ana)
        llenarValido()

        vm.iniciarSesion()

        assertTrue(vm.uiState.value.loginExitoso)
        assertFalse(vm.uiState.value.cargando)
        verify(exactly = 1) { sesion.guardar(1L) }
    }

    @Test
    fun iniciarSesion_credencialesIncorrectas_muestraElMensajeDelBackend() {
        coEvery { repository.autenticar(any(), any()) } returns
                Respuesta.Error("Correo o contraseña incorrectos", 401)
        llenarValido()

        vm.iniciarSesion()

        assertEquals("Correo o contraseña incorrectos", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.loginExitoso)
        assertFalse(vm.uiState.value.cargando)
        verify(exactly = 0) { sesion.guardar(any()) }
    }
}