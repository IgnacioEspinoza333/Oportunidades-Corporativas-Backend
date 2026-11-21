package com.example.appproyecto.data.remote

import com.example.appproyecto.util.SesionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Crea el servicio de Retrofit que usan los repositorios para hablar con el backend. */
object ApiClient {

    fun crearServicio(sesion: SesionManager): ApiService {

        // Agrega "X-Usuario-Id" a cada petición si hay una sesión iniciada.
        // En el login todavía no hay sesión, así que ahí no se envía.
        val encabezadoUsuario = Interceptor { cadena ->
            val usuarioId = sesion.usuarioId()
            val peticion = if (usuarioId != null) {
                cadena.request().newBuilder()
                    .header("X-Usuario-Id", usuarioId.toString())
                    .build()
            } else {
                cadena.request()
            }
            cadena.proceed(peticion)
        }

        // Muestra en Logcat el método, la URL y el código de cada petición.
        // Nivel BASIC: no imprime el cuerpo, así no se filtran contraseñas en el registro.
        val registro = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(encabezadoUsuario)
            .addInterceptor(registro)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.URL_BACKEND)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create()) // JSON <-> objetos Kotlin
            .build()
            .create(ApiService::class.java)
    }
}