package com.example.appproyecto.data.remote

/** Direcciones de los servicios a los que se conecta la app. */
object ApiConfig {

    // Backend propio (Spring Boot). Debe terminar en "/".
    // - Emulador de Android Studio: 10.0.2.2 es el "localhost" del computador.
    // - Teléfono físico: reemplazar por la IP del computador en el WiFi, ej. "http://192.168.1.50:8080/"
    const val URL_BACKEND = "http://10.0.2.2:8080/"
}