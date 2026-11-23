package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Seguimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeosSeguimientoTest {

    @Test
    fun `la gestion se convierte a solicitud con sus datos`() {
        val seguimiento = Seguimiento(
            oportunidadId = 4, usuarioId = 0, contactoId = 8, tipoAccion = "LLAMADA",
            resultado = "Se acordó enviar propuesta", proximaAccion = "Enviar propuesta",
            fechaProximoSeguimiento = 5000L, observaciones = "Prefiere correo"
        )
        val request = seguimiento.aRequest()
        assertEquals(8L, request.contactoId)
        assertEquals("LLAMADA", request.tipoAccion)
        assertEquals("Se acordó enviar propuesta", request.resultado)
        assertEquals(5000L, request.fechaProximoSeguimiento)
    }

    @Test
    fun `una gestion sin contacto ni fecha envia esos campos vacios`() {
        val request = Seguimiento(oportunidadId = 4, usuarioId = 0, resultado = "Revisión interna").aRequest()
        assertNull(request.contactoId)
        assertNull(request.fechaProximoSeguimiento)
    }
}