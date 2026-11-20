package com.example.backend_oportunidades.model

import jakarta.persistence.*

@Entity
@Table(name = "organizaciones")
class Organizacion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var nombre: String = "",

    @Enumerated(EnumType.STRING)
    var tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,

    var rubro: String = "",
    var comuna: String = "",
    var region: String = "",
    var sitioWeb: String = "",
    var publicoAsociado: String = "",

    @Column(length = 1000)
    var observaciones: String = ""
)