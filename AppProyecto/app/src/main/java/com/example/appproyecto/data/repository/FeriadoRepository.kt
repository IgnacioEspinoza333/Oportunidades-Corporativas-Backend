package com.example.appproyecto.data.repository

import com.example.appproyecto.data.model.Feriado
import com.example.appproyecto.data.remote.FeriadosService
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.remote.aFeriado
import com.example.appproyecto.data.remote.llamarApi
import com.example.appproyecto.util.anioDe
import com.example.appproyecto.util.proximosFeriados

/** Feriados de Chile, consultados a una API externa. */
class FeriadoRepository(private val api: FeriadosService) {

    /**
     * Los próximos [maximo] feriados. Si en lo que queda del año hay menos
     * (por ejemplo, en diciembre), también se consulta el año siguiente.
     */
    suspend fun proximos(
        maximo: Int = 3,
        ahora: Long = System.currentTimeMillis()
    ): Respuesta<List<Feriado>> = llamarApi {
        val anio = anioDe(ahora)
        var feriados = api.feriados(anio).mapNotNull { it.aFeriado() }
        var proximos = proximosFeriados(feriados, ahora, maximo)
        if (proximos.size < maximo) {
            feriados = feriados + api.feriados(anio + 1).mapNotNull { it.aFeriado() }
            proximos = proximosFeriados(feriados, ahora, maximo)
        }
        proximos
    }
}