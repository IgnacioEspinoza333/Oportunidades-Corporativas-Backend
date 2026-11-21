package com.example.backend_oportunidades.util

import java.security.MessageDigest

// Convierte un texto en su "huella" SHA-256. Así no se guardan contraseñas en texto plano.
fun sha256(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }