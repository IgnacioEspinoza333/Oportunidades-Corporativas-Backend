package com.example.backend_oportunidades.model

import jakarta.persistence.*

@Entity
@Table(name = "contactos")
class Contacto(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // Un contacto pertenece a una organización
    @ManyToOne(optional = false)
    @JoinColumn(name = "organizacion_id")
    var organizacion: Organizacion,

    var nombre: String = "",
    var cargo: String = "",

    @Enumerated(EnumType.STRING)
    var area: AreaContacto = AreaContacto.EVENTOS,

    var correo: String = "",
    var telefono: String = "",
    var medioPreferente: String = "Correo",
    var estado: String = "Activo",
    var fechaUltimoContacto: Long? = null,

    @Column(length = 1000)
    var observaciones: String = ""
)