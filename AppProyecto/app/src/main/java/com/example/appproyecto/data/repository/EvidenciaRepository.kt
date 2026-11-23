package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EvidenciaDao
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.remote.Respuesta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Las evidencias siguen las mismas reglas de acceso que la oportunidad a la que pertenecen:
 * se le pregunta al backend si el usuario puede ver la oportunidad (responde 403/404 si no).
 */
class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao,
    private val oportunidades: OportunidadApiRepository
) {

    /** Vacío si la oportunidad no existe o el usuario no tiene permiso para verla. */
    fun observarPara(oportunidadId: Long): Flow<List<EvidenciaItem>> = flow {
        if (oportunidades.detalle(oportunidadId) is Respuesta.Exito) {
            emitAll(evidenciaDao.observarItemsPorOportunidad(oportunidadId))
        } else {
            emit(emptyList())
        }
    }

    suspend fun agregar(
        actor: Usuario,
        oportunidadId: Long,
        rutaFoto: String,
        descripcion: String
    ): Resultado {
        when (val acceso = oportunidades.detalle(oportunidadId)) {
            is Respuesta.Exito -> Unit
            is Respuesta.Error -> {
                val sinPermiso = acceso.codigoHttp == 403 || acceso.codigoHttp == 404
                return Resultado.Error(if (sinPermiso) "No tienes acceso a esta oportunidad" else acceso.mensaje)
            }
        }
        if (descripcion.trim().length > DESCRIPCION_MAXIMA) {
            return Resultado.Error("La descripción no puede superar $DESCRIPCION_MAXIMA caracteres")
        }
        if (!File(rutaFoto).exists()) {
            return Resultado.Error("No se encontró la fotografía, intenta tomarla de nuevo")
        }
        evidenciaDao.insertar(
            Evidencia(
                oportunidadId = oportunidadId,
                usuarioId = actor.id,
                usuarioNombre = actor.nombre,
                rutaFoto = rutaFoto,
                fecha = System.currentTimeMillis(),
                descripcion = descripcion.trim()
            )
        )
        return Resultado.Exito("Evidencia guardada")
    }

    companion object {
        const val DESCRIPCION_MAXIMA = 100
    }
}