package com.example.backend_oportunidades.dto

import com.example.backend_oportunidades.model.*

// El usuario NUNCA incluye el passwordHash
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val perfil: Perfil,
    val area: String,
    val zona: String,
    val activo: Boolean
)

data class OrganizacionDto(
    val id: Long,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String,
    val publicoAsociado: String,
    val observaciones: String
)

data class ContactoDto(
    val id: Long,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferente: String,
    val estado: String,
    val fechaUltimoContacto: Long?,
    val observaciones: String
)

data class EventoDto(
    val id: Long,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String,
    val direccion: String,
    val comuna: String,
    val region: String,
    val organizacionId: Long,
    val organizacionNombre: String,
    val publicoEsperado: Int,
    val publicoObjetivo: String,
    val condiciones: String,
    val costoReferencial: Int,
    val observaciones: String
)

// Fila del listado de oportunidades (une oportunidad, evento, organización y responsable)
data class OportunidadItemDto(
    val id: Long,
    val nombre: String,
    val estado: EstadoOportunidad,
    val prioridad: Prioridad,
    val potencial: Int,
    val eventoNombre: String,
    val eventoFecha: Long,
    val comuna: String,
    val organizacionNombre: String,
    val responsableId: Long,
    val responsableNombre: String
)

// Todo lo necesario para la pantalla de detalle
data class OportunidadDetalleDto(
    val id: Long,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuente: String,
    val fechaDeteccion: Long,
    val prioridad: Prioridad,
    val potencial: Int,
    val afinidad: Int,
    val estado: EstadoOportunidad,
    val fechaLimitePostulacion: Long?,
    val fechaActualizacion: Long,
    val observaciones: String,
    val responsableId: Long,
    val responsableNombre: String,
    val eventoId: Long,
    val eventoNombre: String,
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String,
    val direccion: String,
    val comuna: String,
    val region: String,
    val publicoEsperado: Int,
    val publicoObjetivo: String,
    val condiciones: String,
    val costoReferencial: Int,
    val organizacionId: Long,
    val organizacionNombre: String
)

// Una gestión del historial, con el nombre de quien la registró
data class SeguimientoItemDto(
    val id: Long,
    val tipoAccion: TipoAccion,
    val fechaHora: Long,
    val resultado: String,
    val proximaAccion: String,
    val fechaProximoSeguimiento: Long?,
    val estadoNuevo: EstadoOportunidad?,
    val observaciones: String,
    val usuarioNombre: String,
    val contactoNombre: String?
)

// Próxima gestión programada (alimenta las alertas del inicio)
data class SeguimientoPendienteDto(
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val estado: EstadoOportunidad,
    val responsableId: Long,
    val responsableNombre: String,
    val proximaAccion: String,
    val fecha: Long
)