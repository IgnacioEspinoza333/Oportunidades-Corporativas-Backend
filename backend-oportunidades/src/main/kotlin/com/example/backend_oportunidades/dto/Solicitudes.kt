package com.example.backend_oportunidades.dto

import com.example.backend_oportunidades.model.*
import jakarta.validation.constraints.*

// Expresiones regulares: son las mismas que usa Validaciones.kt en la app
object Patrones {
    const val CORREO = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$"""

    // Vacío, o entre 9 y 11 dígitos (se permiten +, espacios, paréntesis y guiones)
    const val TELEFONO_OPCIONAL = """^$|^([+() -]*\d){9,11}[+() -]*$"""

    // Vacío, o un sitio web como www.empresa.cl
    const val SITIO_WEB_OPCIONAL = """^$|^(https?://)?([A-Za-z0-9-]+\.)+[A-Za-z]{2,}(/\S*)?$"""
}

// ---------- Acceso ----------

data class LoginRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Pattern(regexp = Patrones.CORREO, message = "Ingresa un correo válido (ej: nombre@empresa.cl)")
    val correo: String = "",

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    val password: String = ""
)

// ---------- Directorio ----------

data class OrganizacionRequest(
    @field:NotBlank(message = "El nombre de la organización es obligatorio")
    @field:Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    val nombre: String = "",

    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,

    @field:Size(max = 100, message = "El rubro no puede superar 100 caracteres")
    val rubro: String = "",

    @field:Size(max = 100, message = "La comuna no puede superar 100 caracteres")
    val comuna: String = "",

    @field:Size(max = 100, message = "La región no puede superar 100 caracteres")
    val region: String = "",

    @field:Size(max = 100, message = "El sitio web no puede superar 100 caracteres")
    @field:Pattern(regexp = Patrones.SITIO_WEB_OPCIONAL, message = "Ingresa un sitio web válido (ej: www.empresa.cl)")
    val sitioWeb: String = "",

    @field:Size(max = 300, message = "El público asociado no puede superar 300 caracteres")
    val publicoAsociado: String = "",

    @field:Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    val observaciones: String = ""
)

data class ContactoRequest(
    @field:NotBlank(message = "El nombre del contacto es obligatorio")
    @field:Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    val nombre: String = "",

    @field:Size(max = 80, message = "El cargo no puede superar 80 caracteres")
    val cargo: String = "",

    val area: AreaContacto = AreaContacto.EVENTOS,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Pattern(regexp = Patrones.CORREO, message = "Ingresa un correo válido (ej: nombre@empresa.cl)")
    val correo: String = "",

    @field:Pattern(regexp = Patrones.TELEFONO_OPCIONAL, message = "El teléfono debe tener entre 9 y 11 dígitos")
    val telefono: String = "",

    @field:Size(max = 30, message = "El medio preferente no puede superar 30 caracteres")
    val medioPreferente: String = "Correo",

    @field:Size(max = 30, message = "El estado no puede superar 30 caracteres")
    val estado: String = "Activo",

    @field:Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    val observaciones: String = ""
)

// ---------- Eventos ----------

data class EventoRequest(
    @field:NotBlank(message = "El nombre del evento es obligatorio")
    @field:Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    val nombre: String = "",

    val tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,

    @field:Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    val descripcion: String = "",

    // Si falta, queda en 0 y la validación lo rechaza
    @field:Positive(message = "Selecciona la fecha del evento")
    val fechaInicio: Long = 0,

    // Opcional: si no viene, el evento dura un solo día (lo resuelve el servicio)
    val fechaTermino: Long? = null,

    @field:Size(max = 50, message = "El horario no puede superar 50 caracteres")
    val horario: String = "",

    @field:Size(max = 150, message = "La dirección no puede superar 150 caracteres")
    val direccion: String = "",

    @field:Size(max = 100, message = "La comuna no puede superar 100 caracteres")
    val comuna: String = "",

    @field:Size(max = 100, message = "La región no puede superar 100 caracteres")
    val region: String = "",

    @field:Positive(message = "Selecciona una organización")
    val organizacionId: Long = 0,

    @field:Min(value = 1, message = "El público esperado debe ser al menos 1 persona")
    val publicoEsperado: Int = 0,

    @field:Size(max = 150, message = "El público objetivo no puede superar 150 caracteres")
    val publicoObjetivo: String = "",

    @field:Size(max = 300, message = "Las condiciones no pueden superar 300 caracteres")
    val condiciones: String = "",

    @field:Min(value = 0, message = "El costo no puede ser negativo")
    @field:Max(value = 100_000_000, message = "El costo no puede superar $100.000.000")
    val costoReferencial: Int = 0,

    @field:Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    val observaciones: String = ""
)

// ---------- Oportunidades ----------

data class OportunidadRequest(
    @field:NotBlank(message = "El nombre de la oportunidad es obligatorio")
    @field:Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    val nombre: String = "",

    val tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,

    @field:Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    val descripcion: String = "",

    @field:Size(max = 100, message = "La fuente no puede superar 100 caracteres")
    val fuente: String = "",

    @field:Positive(message = "Selecciona un evento")
    val eventoId: Long = 0,

    // Solo lo usa el supervisor; el ejecutivo siempre queda como responsable (lo decide el servicio)
    val responsableId: Long? = null,

    val prioridad: Prioridad = Prioridad.MEDIA,

    @field:Min(value = 1, message = "El potencial debe estar entre 1 y 5")
    @field:Max(value = 5, message = "El potencial debe estar entre 1 y 5")
    val potencial: Int = 3,

    @field:Min(value = 1, message = "La afinidad debe estar entre 1 y 5")
    @field:Max(value = 5, message = "La afinidad debe estar entre 1 y 5")
    val afinidad: Int = 3,

    val fechaLimitePostulacion: Long? = null,

    @field:Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    val observaciones: String = ""
)

data class CambioEstadoRequest(
    val estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,

    @field:Size(max = 300, message = "La observación no puede superar 300 caracteres")
    val observacion: String = ""
)

data class AsignarRequest(
    @field:Positive(message = "Selecciona un ejecutivo")
    val responsableId: Long = 0
)

// ---------- Seguimientos ----------

data class SeguimientoRequest(
    // Opcional, salvo en las acciones que implican hablar con alguien (lo revisa el servicio)
    val contactoId: Long? = null,

    val tipoAccion: TipoAccion = TipoAccion.SEGUIMIENTO,

    @field:NotBlank(message = "El resultado es obligatorio")
    @field:Size(max = 1000, message = "El resultado no puede superar 1000 caracteres")
    val resultado: String = "",

    @field:Size(max = 100, message = "La próxima acción no puede superar 100 caracteres")
    val proximaAccion: String = "",

    val fechaProximoSeguimiento: Long? = null,

    @field:Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    val observaciones: String = ""
)