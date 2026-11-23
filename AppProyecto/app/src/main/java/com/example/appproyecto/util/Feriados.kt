package com.example.appproyecto.util

import com.example.appproyecto.data.model.Feriado
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Convierte "2026-12-25" en la medianoche local de ese día. Devuelve null si el texto no es una fecha válida. */
fun parsearFechaIso(texto: String): Long? =
    try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(texto)?.time
    } catch (e: ParseException) {
        null
    }

/** Año al que pertenece [millis]. */
fun anioDe(millis: Long): Int =
    Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)

/** Los próximos [maximo] feriados desde el día de [desde] (hoy incluido), del más cercano al más lejano. */
fun proximosFeriados(feriados: List<Feriado>, desde: Long, maximo: Int): List<Feriado> =
    feriados
        .filter { it.fecha >= inicioDelDia(desde) }
        .sortedBy { it.fecha }
        .take(maximo)

/** Texto para mostrar cuánto falta: "Hoy", "Mañana" o "En 5 días". */
fun textoDiasHasta(dias: Int): String = when {
    dias <= 0 -> "Hoy"
    dias == 1 -> "Mañana"
    else -> "En $dias días"
}