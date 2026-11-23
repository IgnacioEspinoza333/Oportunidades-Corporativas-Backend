package com.example.appproyecto

import com.example.appproyecto.data.repository.FeriadoRepository
import com.example.appproyecto.data.repository.OportunidadApiRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

/** App y contenedor simulados, para probar los ViewModels que reciben Application. */
class EntornoApp {
    val usuarioRepository = mockk<UsuarioRepository>()
    val oportunidadRepository = mockk<OportunidadApiRepository>()
    val feriadoRepository = mockk<FeriadoRepository>()

    /** Contador que usan los ViewModels para recargarse; súbelo para simular un cambio guardado. */
    val cambios = MutableStateFlow(0)

    val aplicacion: AppProyectoApp = mockk(relaxed = true)

    init {
        val contenedor = mockk<ContenedorApp>()
        every { contenedor.usuarioRepository } returns usuarioRepository
        every { contenedor.oportunidadRepository } returns oportunidadRepository
        every { contenedor.feriadoRepository } returns feriadoRepository
        every { aplicacion.contenedor } returns contenedor
        every { oportunidadRepository.cambios } returns cambios
    }
}