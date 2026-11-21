package com.example.backend_oportunidades.dto

import com.example.backend_oportunidades.model.*

// Funciones de extensión: convierten una entidad en su DTO. Ej: usuario.aDto()

fun Usuario.aDto() = UsuarioDto(id, nombre, correo, perfil, area, zona, activo)

fun Organizacion.aDto() = OrganizacionDto(
    id, nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones
)

fun Contacto.aDto() = ContactoDto(
    id, organizacion.id, nombre, cargo, area, correo, telefono,
    medioPreferente, estado, fechaUltimoContacto, observaciones
)

fun Evento.aDto() = EventoDto(
    id, nombre, tipo, descripcion, fechaInicio, fechaTermino, horario, direccion,
    comuna, region, organizacion.id, organizacion.nombre, publicoEsperado,
    publicoObjetivo, condiciones, costoReferencial, observaciones
)

fun Oportunidad.aItemDto() = OportunidadItemDto(
    id = id,
    nombre = nombre,
    estado = estado,
    prioridad = prioridad,
    potencial = potencial,
    eventoNombre = evento.nombre,
    eventoFecha = evento.fechaInicio,
    comuna = evento.comuna,
    organizacionNombre = evento.organizacion.nombre,
    responsableId = responsable.id,
    responsableNombre = responsable.nombre
)

fun Oportunidad.aDetalleDto() = OportunidadDetalleDto(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fuente = fuente,
    fechaDeteccion = fechaDeteccion,
    prioridad = prioridad,
    potencial = potencial,
    afinidad = afinidad,
    estado = estado,
    fechaLimitePostulacion = fechaLimitePostulacion,
    fechaActualizacion = fechaActualizacion,
    observaciones = observaciones,
    responsableId = responsable.id,
    responsableNombre = responsable.nombre,
    eventoId = evento.id,
    eventoNombre = evento.nombre,
    fechaInicio = evento.fechaInicio,
    fechaTermino = evento.fechaTermino,
    horario = evento.horario,
    direccion = evento.direccion,
    comuna = evento.comuna,
    region = evento.region,
    publicoEsperado = evento.publicoEsperado,
    publicoObjetivo = evento.publicoObjetivo,
    condiciones = evento.condiciones,
    costoReferencial = evento.costoReferencial,
    organizacionId = evento.organizacion.id,
    organizacionNombre = evento.organizacion.nombre
)

fun Seguimiento.aItemDto() = SeguimientoItemDto(
    id = id,
    tipoAccion = tipoAccion,
    fechaHora = fechaHora,
    resultado = resultado,
    proximaAccion = proximaAccion,
    fechaProximoSeguimiento = fechaProximoSeguimiento,
    estadoNuevo = estadoNuevo,
    observaciones = observaciones,
    usuarioNombre = usuario.nombre,
    contactoNombre = contacto?.nombre
)