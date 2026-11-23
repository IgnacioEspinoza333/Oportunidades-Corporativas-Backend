package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Feriado
import com.example.appproyecto.util.parsearFechaIso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/** Un feriado tal como lo entrega la API externa: fecha "2026-12-25", nombre en español y en inglés. */
data class FeriadoDto(
    val date: String,
    val localName: String,
    val name: String
)

/** Endpoint de la API externa (Nager.Date). "CL" es el código de Chile. */
interface FeriadosService {

    @GET("api/v3/PublicHolidays/{anio}/CL")
    suspend fun feriados(@Path("anio") anio: Int): List<FeriadoDto>
}

/** Cliente aparte del de nuestro backend: a un servicio externo NO se le envía X-Usuario-Id. */
object FeriadosClient {

    fun crearServicio(): FeriadosService {
        val registro = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(registro)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.URL_FERIADOS)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeriadosService::class.java)
    }
}

/** Convierte el feriado de la API al modelo de la app. Si la fecha no es válida, se descarta (null). */
fun FeriadoDto.aFeriado(): Feriado? =
    parsearFechaIso(date)?.let { Feriado(fecha = it, nombre = localName.ifBlank { name }) }