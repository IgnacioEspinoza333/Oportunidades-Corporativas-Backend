package com.example.backend_oportunidades.model

import jakarta.persistence.*

@Entity
@Table(name = "eventos")
class Evento(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var nombre: String = "",

    @Enumerated(EnumType.STRING)
    var tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,

    @Column(length = 1000)
    var descripcion: String = "",

    var fechaInicio: Long = 0,
    var fechaTermino: Long = 0,
    var horario: String = "",
    var direccion: String = "",
    var comuna: String = "",
    var region: String = "",

    // Un evento pertenece a una organización
    @ManyToOne(optional = false)
    @JoinColumn(name = "organizacion_id")
    var organizacion: Organizacion,

    var publicoEsperado: Int = 0,
    var publicoObjetivo: String = "",
    var condiciones: String = "",
    var costoReferencial: Int = 0,

    @Column(length = 1000)
    var observaciones: String = ""
)