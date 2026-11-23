package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Fotografía de respaldo de una oportunidad (el stand, el lugar, la activación...).
 * Se guarda en el teléfono, porque el backend no almacena archivos. Es lo único que usa Room.
 * Una oportunidad puede tener varias; nunca se borran, igual que el historial de gestiones.
 */
@Entity(
    tableName = "evidencias",
    indices = [Index("oportunidadId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Id de la oportunidad en el backend. */
    val oportunidadId: Long,
    /** Quién tomó la foto. */
    val usuarioId: Long,
    val usuarioNombre: String,
    /** Ruta del archivo en el almacenamiento interno de la app. */
    val rutaFoto: String,
    val fecha: Long = System.currentTimeMillis(),
    val descripcion: String = ""
)