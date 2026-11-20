package com.example.backend_oportunidades.model

import jakarta.persistence.*

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var nombre: String = "",

    // El correo no se puede repetir
    @Column(unique = true, nullable = false)
    var correo: String = "",

    // Nunca se guarda la contraseña en texto plano, solo su hash
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    var perfil: Perfil = Perfil.EJECUTIVO,

    var area: String = "Comercial",
    var zona: String = "Región Metropolitana",
    var activo: Boolean = true,
    var fechaCreacion: Long = System.currentTimeMillis()
)