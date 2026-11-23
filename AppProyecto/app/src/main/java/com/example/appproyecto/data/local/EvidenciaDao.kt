package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {
    @Insert suspend fun insertar(evidencia: Evidencia): Long

    /** Evidencias de una oportunidad, la más reciente primero. */
    @Query("""
        SELECT id AS id, rutaFoto AS rutaFoto, fecha AS fecha,
               descripcion AS descripcion, usuarioNombre AS usuarioNombre
        FROM evidencias
        WHERE oportunidadId = :oportunidadId
        ORDER BY fecha DESC, id DESC
    """)
    fun observarItemsPorOportunidad(oportunidadId: Long): Flow<List<EvidenciaItem>>
}