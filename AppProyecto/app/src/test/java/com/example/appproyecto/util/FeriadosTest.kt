package com.example.appproyecto.util

import com.example.appproyecto.data.model.Feriado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FeriadosTest {

    private fun feriado(fecha: String, nombre: String) = Feriado(parsearFechaIso(fecha)!!, nombre)

    private val lista = listOf(
        feriado("2026-12-25", "Navidad"),
        feriado("2026-09-18", "Independencia"),
        feriado("2026-12-08", "Inmaculada Concepción"),
        feriado("2026-01-01", "Año Nuevo")
    )

    @Test
    fun `una fecha valida se convierte y una invalida es null`() {
        assertNotNull(parsearFechaIso("2026-12-25"))
        assertNull(parsearFechaIso("25/12/2026"))
        assertNull(parsearFechaIso("2026-13-45"))
    }

    @Test
    fun `los proximos feriados salen ordenados y sin los que ya pasaron`() {
        val desde = parsearFechaIso("2026-09-01")!!
        val proximos = proximosFeriados(lista, desde, 3)
        assertEquals(listOf("Independencia", "Inmaculada Concepción", "Navidad"), proximos.map { it.nombre })
    }

    @Test
    fun `un feriado de hoy se incluye`() {
        val hoy = parsearFechaIso("2026-09-18")!!
        assertEquals("Independencia", proximosFeriados(lista, hoy, 1).first().nombre)
    }

    @Test
    fun `se respeta el maximo pedido`() {
        val desde = parsearFechaIso("2026-01-01")!!
        assertEquals(2, proximosFeriados(lista, desde, 2).size)
    }

    @Test
    fun `el texto de dias hasta el feriado`() {
        assertEquals("Hoy", textoDiasHasta(0))
        assertEquals("Mañana", textoDiasHasta(1))
        assertEquals("En 5 días", textoDiasHasta(5))
    }
}