package com.example.backend_oportunidades.error

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * Atrapa las excepciones de todos los controladores y las convierte en una respuesta
 * ordenada. Así los servicios solo lanzan excepciones y no arman respuestas HTTP.
 */
@RestControllerAdvice
class ManejadorErrores {

    private val log = LoggerFactory.getLogger(ManejadorErrores::class.java)

    private fun respuesta(estado: HttpStatus, mensaje: String, campos: Map<String, String>? = null) =
        ResponseEntity.status(estado).body(ApiError(mensaje, campos))

    // ---------- Errores de negocio ----------

    @ExceptionHandler(RecursoNoEncontradoException::class)
    fun noEncontrado(e: RecursoNoEncontradoException) =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "No encontrado")

    @ExceptionHandler(ReglaNegocioException::class)
    fun reglaNegocio(e: ReglaNegocioException) =
        respuesta(HttpStatus.BAD_REQUEST, e.message ?: "Solicitud inválida")

    @ExceptionHandler(ConflictoException::class)
    fun conflicto(e: ConflictoException) =
        respuesta(HttpStatus.CONFLICT, e.message ?: "Conflicto con datos existentes")

    @ExceptionHandler(AccesoDenegadoException::class)
    fun accesoDenegado(e: AccesoDenegadoException) =
        respuesta(HttpStatus.FORBIDDEN, e.message ?: "Acceso denegado")

    @ExceptionHandler(NoAutenticadoException::class)
    fun noAutenticado(e: NoAutenticadoException) =
        respuesta(HttpStatus.UNAUTHORIZED, e.message ?: "No autenticado")

    // ---------- Errores de validación y de formato ----------

    // Falla alguna anotación (@NotBlank, @Size...) del cuerpo de la petición
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun datosInvalidos(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val campos = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor inválido") }
        // Como mensaje principal se muestra el primer error
        val mensaje = campos.values.firstOrNull() ?: "Hay datos inválidos"
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, campos)
    }

    // El JSON está mal formado o trae un valor que no existe en un enum
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun cuerpoInvalido(e: HttpMessageNotReadableException) =
        respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido")

    // Por ejemplo, /oportunidades/abc cuando se esperaba un número
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun parametroInvalido(e: MethodArgumentTypeMismatchException) =
        respuesta(HttpStatus.BAD_REQUEST, "El parámetro '${e.name}' no es válido")

    // Falta el encabezado que identifica al usuario
    @ExceptionHandler(MissingRequestHeaderException::class)
    fun faltaEncabezado(e: MissingRequestHeaderException) =
        respuesta(HttpStatus.UNAUTHORIZED, "Falta el encabezado ${e.headerName}")

    // ---------- Errores de la base de datos y del servidor ----------

    // Por ejemplo, un correo repetido que viola la restricción "unique"
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integridad(e: DataIntegrityViolationException) =
        respuesta(HttpStatus.CONFLICT, "Los datos chocan con otro registro existente")

    @ExceptionHandler(NoResourceFoundException::class)
    fun rutaInexistente(e: NoResourceFoundException) =
        respuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun metodoNoPermitido(e: HttpRequestMethodNotSupportedException) =
        respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido en esta ruta")

    // Cualquier otro error inesperado: se registra y se responde algo genérico
    @ExceptionHandler(Exception::class)
    fun inesperado(e: Exception): ResponseEntity<ApiError> {
        log.error("Error inesperado", e)
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor")
    }
}