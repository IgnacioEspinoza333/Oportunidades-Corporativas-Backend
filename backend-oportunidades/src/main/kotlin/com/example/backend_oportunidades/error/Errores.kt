package com.example.backend_oportunidades.error

// Forma única de toda respuesta de error. La app Android muestra el campo "mensaje".
data class ApiError(
    val mensaje: String,
    // Detalle por campo cuando falla una validación (campo -> mensaje)
    val campos: Map<String, String>? = null
)

// Cada excepción representa un tipo de error y se convierte en un código HTTP.

/** 404: el recurso no existe. */
class RecursoNoEncontradoException(mensaje: String) : RuntimeException(mensaje)

/** 400: se rompe una regla de negocio. */
class ReglaNegocioException(mensaje: String) : RuntimeException(mensaje)

/** 409: el dato choca con otro que ya existe (duplicados). */
class ConflictoException(mensaje: String) : RuntimeException(mensaje)

/** 403: el usuario no tiene permiso para esa acción. */
class AccesoDenegadoException(mensaje: String) : RuntimeException(mensaje)

/** 401: credenciales incorrectas o usuario no identificado. */
class NoAutenticadoException(mensaje: String) : RuntimeException(mensaje)