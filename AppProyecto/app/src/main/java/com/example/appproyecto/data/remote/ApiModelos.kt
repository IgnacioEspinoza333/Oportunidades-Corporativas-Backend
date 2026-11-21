package com.example.appproyecto.data.remote

// ---------- Solicitudes (lo que la app envía al backend) ----------

data class LoginRequest(
    val correo: String,
    val password: String
)

data class OrganizacionRequest(
    val nombre: String,
    val tipo: String,
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)

data class ContactoRequest(
    val nombre: String,
    val cargo: String = "",
    val area: String,
    val correo: String,
    val telefono: String = "",
    val medioPreferente: String = "Correo",
    val estado: String = "Activo",
    val observaciones: String = ""
)

data class EventoRequest(
    val nombre: String,
    val tipo: String,
    val descripcion: String = "",
    val fechaInicio: Long,
    val fechaTermino: Long? = null,
    val horario: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val region: String = "",
    val organizacionId: Long,
    val publicoEsperado: Int,
    val publicoObjetivo: String = "",
    val condiciones: String = "",
    val costoReferencial: Int = 0,
    val observaciones: String = ""
)

data class OportunidadRequest(
    val nombre: String,
    val tipo: String,
    val descripcion: String = "",
    val fuente: String = "",
    val eventoId: Long,
    val responsableId: Long? = null,
    val prioridad: String,
    val potencial: Int,
    val afinidad: Int,
    val fechaLimitePostulacion: Long? = null,
    val observaciones: String = ""
)

data class CambioEstadoRequest(
    val estado: String,
    val observacion: String = ""
)

data class AsignarRequest(
    val responsableId: Long
)

data class SeguimientoRequest(
    val contactoId: Long? = null,
    val tipoAccion: String,
    val resultado: String,
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null,
    val observaciones: String = ""
)

// ---------- Respuestas (lo que el backend devuelve) ----------

// El backend nunca envía la contraseña
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val perfil: String,
    val area: String,
    val zona: String,
    val activo: Boolean
)

data class OrganizacionDto(
    val id: Long,
    val nombre: String,
    val tipo: String,
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
    val area: String,
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
    val tipo: String,
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

// Forma de todo error que devuelve el backend: { "mensaje": "...", "campos": { ... } }
data class ErrorApi(
    val mensaje: String? = null,
    val campos: Map<String, String>? = null
)