package com.example.appproyecto.data.model

data class Evento(
    val id: Long = 0,
    val nombre: String,
    val tipo: String = TipoOportunidad.EVENTO_CORPORATIVO.name,
    val descripcion: String = "",
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val region: String = "",
    val organizacionId: Long,
    val publicoEsperado: Int = 0,
    val publicoObjetivo: String = "",
    val condiciones: String = "",
    val costoReferencial: Int = 0,
    val observaciones: String = "",
    /** Ruta de la foto guardada en el teléfono (el backend no almacena fotos). */
    val fotoUri: String? = null
)