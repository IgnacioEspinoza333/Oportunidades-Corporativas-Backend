package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.Usuario

// Conversión de los datos del backend (DTO) a los modelos que usa la app.

fun UsuarioDto.aUsuario() = Usuario(
    id = id,
    nombre = nombre,
    correo = correo,
    passwordHash = "", // el backend nunca envía la contraseña
    perfil = perfil,
    area = area,
    zona = zona,
    activo = activo
)