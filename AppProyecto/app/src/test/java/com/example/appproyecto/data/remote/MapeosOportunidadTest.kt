package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Oportunidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeosOportunidadTest {

    @Test
    fun `sin responsable elegido no se envia responsable al backend`() {
        val oportunidad = Oportunidad(nombre = "Feria Andina", eventoId = 2, responsableId = 0)
        assertNull(oportunidad.aRequest().responsableId)
    }

    @Test
    fun `con responsable elegido se envia su id`() {
        val oportunidad = Oportunidad(nombre = "Feria Andina", eventoId = 2, responsableId = 3)
        assertEquals(3L, oportunidad.aRequest().responsableId)
    }

    @Test
    fun `la solicitud conserva prioridad puntajes y evento`() {
        val oportunidad = Oportunidad(
            nombre = "Expo", eventoId = 9, responsableId = 1,
            prioridad = "ALTA", potencial = 5, afinidad = 4
        )
        val request = oportunidad.aRequest()
        assertEquals("ALTA", request.prioridad)
        assertEquals(5, request.potencial)
        assertEquals(4, request.afinidad)
        assertEquals(9L, request.eventoId)
    }
}