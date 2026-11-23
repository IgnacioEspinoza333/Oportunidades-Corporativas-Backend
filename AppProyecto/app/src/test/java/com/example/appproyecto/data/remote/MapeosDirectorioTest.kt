package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import org.junit.Assert.assertEquals
import org.junit.Test

class MapeosDirectorioTest {

    @Test
    fun `organizacion se convierte a request con sus datos`() {
        val org = Organizacion(id = 5, nombre = "Acme", tipo = "EMPRESA", rubro = "Retail")
        val request = org.aRequest()
        assertEquals("Acme", request.nombre)
        assertEquals("EMPRESA", request.tipo)
        assertEquals("Retail", request.rubro)
    }

    @Test
    fun `dto de organizacion se convierte al modelo de la app`() {
        val dto = OrganizacionDto(
            id = 7, nombre = "Duoc", tipo = "EMPRESA", rubro = "Educación",
            comuna = "Santiago", region = "RM", sitioWeb = "", publicoAsociado = "", observaciones = ""
        )
        val org = dto.aOrganizacion()
        assertEquals(7L, org.id)
        assertEquals("EMPRESA", org.tipo)
        assertEquals("Santiago", org.comuna)
    }

    @Test
    fun `contacto se convierte a request con area y correo`() {
        val contacto = Contacto(id = 1, organizacionId = 2, nombre = "Ana", area = "EVENTOS", correo = "a@b.cl")
        val request = contacto.aRequest()
        assertEquals("EVENTOS", request.area)
        assertEquals("a@b.cl", request.correo)
    }
}