package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Evento
import org.junit.Assert.assertEquals
import org.junit.Test

class MapeosEventoTest {

    @Test
    fun `evento se convierte a request con sus datos`() {
        val evento = Evento(
            nombre = "Feria Andina", tipo = "EVENTO_CORPORATIVO", fechaInicio = 1000L, fechaTermino = 2000L,
            organizacionId = 3, publicoEsperado = 150, costoReferencial = 50000
        )
        val request = evento.aRequest()
        assertEquals("Feria Andina", request.nombre)
        assertEquals("EVENTO_CORPORATIVO", request.tipo)
        assertEquals(3L, request.organizacionId)
        assertEquals(150, request.publicoEsperado)
    }
}