package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Usuario

fun UsuarioDto.aUsuario() = Usuario(
    id = id,
    nombre = nombre,
    correo = correo,
    passwordHash = "", // el backend nunca envía la contraseña
    perfil = perfil,
    area = area,
    zona = zona,
    activo = activo
)

// ---------- Organizaciones ----------
fun OrganizacionDto.aOrganizacion() = Organizacion(
    id = id,
    nombre = nombre,
    tipo = tipo,
    rubro = rubro,
    comuna = comuna,
    region = region,
    sitioWeb = sitioWeb,
    publicoAsociado = publicoAsociado,
    observaciones = observaciones
)

fun Organizacion.aRequest() = OrganizacionRequest(
    nombre = nombre,
    tipo = tipo,
    rubro = rubro,
    comuna = comuna,
    region = region,
    sitioWeb = sitioWeb,
    publicoAsociado = publicoAsociado,
    observaciones = observaciones
)

// ---------- Contactos ----------
fun ContactoDto.aContacto() = Contacto(
    id = id,
    organizacionId = organizacionId,
    nombre = nombre,
    cargo = cargo,
    area = area,
    correo = correo,
    telefono = telefono,
    medioPreferente = medioPreferente,
    estado = estado,
    fechaUltimoContacto = fechaUltimoContacto,
    observaciones = observaciones
)

fun Contacto.aRequest() = ContactoRequest(
    nombre = nombre,
    cargo = cargo,
    area = area,
    correo = correo,
    telefono = telefono,
    medioPreferente = medioPreferente,
    estado = estado,
    observaciones = observaciones
)

// ---------- Eventos ----------
fun EventoDto.aEvento(fotoUri: String?) = Evento(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fechaInicio = fechaInicio,
    fechaTermino = fechaTermino,
    horario = horario,
    direccion = direccion,
    comuna = comuna,
    region = region,
    organizacionId = organizacionId,
    publicoEsperado = publicoEsperado,
    publicoObjetivo = publicoObjetivo,
    condiciones = condiciones,
    costoReferencial = costoReferencial,
    observaciones = observaciones,
    fotoUri = fotoUri // viene del almacén local, no del backend
)

fun Evento.aRequest() = EventoRequest(
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fechaInicio = fechaInicio,
    fechaTermino = fechaTermino,
    horario = horario,
    direccion = direccion,
    comuna = comuna,
    region = region,
    organizacionId = organizacionId,
    publicoEsperado = publicoEsperado,
    publicoObjetivo = publicoObjetivo,
    condiciones = condiciones,
    costoReferencial = costoReferencial,
    observaciones = observaciones
)

// ---------- Oportunidades ----------

/** Datos editables de una oportunidad, a partir del detalle que entrega el backend. */
fun OportunidadDetalle.aOportunidad() = Oportunidad(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fuente = fuente,
    fechaDeteccion = fechaDeteccion,
    eventoId = eventoId,
    responsableId = responsableId,
    prioridad = prioridad,
    potencial = potencial,
    afinidad = afinidad,
    estado = estado,
    fechaLimitePostulacion = fechaLimitePostulacion,
    fechaActualizacion = fechaActualizacion,
    observaciones = observaciones
)

/** responsableId = 0 significa "sin responsable elegido": no se envía y el backend decide. */
fun Oportunidad.aRequest() = OportunidadRequest(
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fuente = fuente,
    eventoId = eventoId,
    responsableId = responsableId.takeIf { it != 0L },
    prioridad = prioridad,
    potencial = potencial,
    afinidad = afinidad,
    fechaLimitePostulacion = fechaLimitePostulacion,
    observaciones = observaciones
)