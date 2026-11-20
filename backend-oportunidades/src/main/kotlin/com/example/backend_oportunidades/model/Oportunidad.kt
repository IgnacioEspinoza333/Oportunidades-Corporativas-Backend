package com.example.backend_oportunidades.model

import jakarta.persistence.*

@Entity
@Table(name = "oportunidades")
class Oportunidad(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var nombre: String = "",

    @Enumerated(EnumType.STRING)
    var tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,

    @Column(length = 1000)
    var descripcion: String = "",

    var fuente: String = "",
    var fechaDeteccion: Long = System.currentTimeMillis(),

    // Una oportunidad nace de un evento y tiene un usuario responsable
    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id")
    var evento: Evento,

    @ManyToOne(optional = false)
    @JoinColumn(name = "responsable_id")
    var responsable: Usuario,

    @Enumerated(EnumType.STRING)
    var prioridad: Prioridad = Prioridad.MEDIA,

    var potencial: Int = 3,
    var afinidad: Int = 3,

    @Enumerated(EnumType.STRING)
    var estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,

    var fechaLimitePostulacion: Long? = null,
    var fechaActualizacion: Long = System.currentTimeMillis(),

    @Column(length = 1000)
    var observaciones: String = ""
)