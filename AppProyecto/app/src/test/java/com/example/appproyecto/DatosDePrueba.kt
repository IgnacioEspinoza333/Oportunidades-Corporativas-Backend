package com.example.appproyecto

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad

fun itemPrueba(
    id: Long,
    nombre: String,
    estado: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE,
    prioridad: Prioridad = Prioridad.MEDIA
) = OportunidadItem(
    id = id, nombre = nombre, estado = estado.name, prioridad = prioridad.name, potencial = 3,
    eventoNombre = "Evento $id", eventoFecha = System.currentTimeMillis() + 5 * 86_400_000L,
    comuna = "Santiago", organizacionNombre = "Org $id", responsableId = 1, responsableNombre = "Ana (demo)"
)

fun detallePrueba(
    estado: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE,
    responsableId: Long = 1L
) = OportunidadDetalle(
    id = 10, nombre = "Feria Andina", tipo = "EVENTO_CORPORATIVO", descripcion = "", fuente = "",
    fechaDeteccion = 1000L, prioridad = "MEDIA", potencial = 3, afinidad = 3, estado = estado.name,
    fechaLimitePostulacion = null, fechaActualizacion = 1000L, observaciones = "",
    responsableId = responsableId, responsableNombre = "Ana (demo)", eventoId = 1,
    eventoNombre = "Feria 2026", fechaInicio = 2000L, fechaTermino = 3000L, horario = "10:00",
    direccion = "Calle 1", comuna = "Santiago", region = "Metropolitana", publicoEsperado = 100,
    publicoObjetivo = "", condiciones = "", costoReferencial = 0, fotoUri = null,
    organizacionId = 3, organizacionNombre = "Org"
)