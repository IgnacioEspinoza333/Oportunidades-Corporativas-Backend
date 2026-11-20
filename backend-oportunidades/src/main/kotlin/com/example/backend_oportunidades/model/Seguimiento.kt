package com.example.backend_oportunidades.model

import jakarta.persistence.*

// Una gestión sobre una oportunidad (llamada, reunión, propuesta...). Forma el historial.
@Entity
@Table(name = "seguimientos")
class Seguimiento(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "oportunidad_id")
    var oportunidad: Oportunidad,

    // Quién registró la gestión
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    var usuario: Usuario,

    // Contacto con el que se habló (es opcional)
    @ManyToOne
    @JoinColumn(name = "contacto_id")
    var contacto: Contacto? = null,

    @Enumerated(EnumType.STRING)
    var tipoAccion: TipoAccion = TipoAccion.SEGUIMIENTO,

    var fechaHora: Long = System.currentTimeMillis(),

    @Column(length = 1000)
    var resultado: String = "",

    var proximaAccion: String = "",
    var fechaProximoSeguimiento: Long? = null,

    // Si esta gestión cambió el estado, queda registrado aquí
    @Enumerated(EnumType.STRING)
    var estadoNuevo: EstadoOportunidad? = null,

    @Column(length = 1000)
    var observaciones: String = ""
)