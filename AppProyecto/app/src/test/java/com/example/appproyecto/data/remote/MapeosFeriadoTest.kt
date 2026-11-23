package com.example.appproyecto.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeosFeriadoTest {

    @Test
    fun `el feriado usa el nombre en espanol`() {
        val feriado = FeriadoDto(date = "2026-12-25", localName = "Navidad", name = "Christmas Day").aFeriado()
        assertEquals("Navidad", feriado?.nombre)
    }

    @Test
    fun `si no hay nombre local se usa el nombre en ingles`() {
        val feriado = FeriadoDto(date = "2026-12-25", localName = "", name = "Christmas Day").aFeriado()
        assertEquals("Christmas Day", feriado?.nombre)
    }

    @Test
    fun `un feriado con fecha invalida se descarta`() {
        assertNull(FeriadoDto(date = "sin fecha", localName = "X", name = "X").aFeriado())
    }
}