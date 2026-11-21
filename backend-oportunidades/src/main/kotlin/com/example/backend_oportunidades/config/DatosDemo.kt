package com.example.backend_oportunidades.config

import com.example.backend_oportunidades.model.*
import com.example.backend_oportunidades.repository.*
import com.example.backend_oportunidades.util.sha256
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

private const val DIA = 86_400_000L

/** Fecha relativa a hoy: dias(3) = en 3 días, dias(-2) = hace 2 días. */
private fun dias(n: Int): Long = System.currentTimeMillis() + n * DIA

/**
 * Carga datos 100 % ficticios para probar la API.
 * Se ejecuta al iniciar el servidor, pero solo si la base de datos está vacía.
 */
@Component
class DatosDemo(
    private val usuarios: UsuarioRepository,
    private val organizaciones: OrganizacionRepository,
    private val contactos: ContactoRepository,
    private val eventos: EventoRepository,
    private val oportunidades: OportunidadRepository,
    private val seguimientos: SeguimientoRepository
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (usuarios.count() > 0) return

        // ---------- Usuarios: 3 ejecutivos y 1 supervisor ----------
        val ana = usuarios.save(Usuario(
            nombre = "Ana Pérez (demo)", correo = "ana.ejecutiva@ejemplo.cl",
            passwordHash = sha256("Ana12345"), perfil = Perfil.EJECUTIVO, zona = "Región Metropolitana"))
        val carlos = usuarios.save(Usuario(
            nombre = "Carlos Soto (demo)", correo = "carlos.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Carlos123"), perfil = Perfil.EJECUTIVO, zona = "Valparaíso"))
        val diego = usuarios.save(Usuario(
            nombre = "Diego Fuentes (demo)", correo = "diego.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Diego1234"), perfil = Perfil.EJECUTIVO, zona = "Biobío"))
        usuarios.save(Usuario(
            nombre = "Sofía Rojas (demo)", correo = "sofia.supervisora@ejemplo.cl",
            passwordHash = sha256("Sofia1234"), perfil = Perfil.SUPERVISOR, area = "Supervisión"))

        // ---------- Organizaciones ----------
        val muni = organizaciones.save(Organizacion(
            nombre = "Municipalidad de Valle Verde", tipo = TipoOrganizacion.MUNICIPALIDAD,
            rubro = "Gobierno local", comuna = "Puente Alto", region = "Metropolitana",
            publicoAsociado = "Vecinos y funcionarios municipales"))
        val feria = organizaciones.save(Organizacion(
            nombre = "Corporación Feria Andina", tipo = TipoOrganizacion.ORGANIZADOR_EVENTOS,
            rubro = "Ferias y eventos", comuna = "Santiago", region = "Metropolitana",
            publicoAsociado = "Empresas y familias"))
        val holding = organizaciones.save(Organizacion(
            nombre = "Holding Alfa Demo S.A.", tipo = TipoOrganizacion.EMPRESA,
            rubro = "Retail", comuna = "Las Condes", region = "Metropolitana",
            publicoAsociado = "Trabajadores y sus familias"))
        val plaza = organizaciones.save(Organizacion(
            nombre = "Centro Comercial Plaza Demo", tipo = TipoOrganizacion.CENTRO_COMERCIAL,
            rubro = "Comercio", comuna = "Viña del Mar", region = "Valparaíso",
            publicoAsociado = "Visitantes y clientes"))
        val universidad = organizaciones.save(Organizacion(
            nombre = "Instituto Demo del Sur", tipo = TipoOrganizacion.INSTITUCION_PRIVADA,
            rubro = "Educación", comuna = "Concepción", region = "Biobío",
            publicoAsociado = "Estudiantes y funcionarios"))

        // ---------- Contactos ----------
        val c1 = contactos.save(Contacto(
            organizacion = muni, nombre = "Marta Lagos (demo)", cargo = "Encargada de Bienestar",
            area = AreaContacto.BIENESTAR, correo = "marta.lagos@ejemplo.cl",
            telefono = "+56 9 0000 0001", fechaUltimoContacto = dias(-4)))
        val c2 = contactos.save(Contacto(
            organizacion = feria, nombre = "Rodrigo Vega (demo)", cargo = "Jefe de Producción",
            area = AreaContacto.EVENTOS, correo = "rodrigo.vega@ejemplo.cl",
            telefono = "+56 9 0000 0002", fechaUltimoContacto = dias(-8)))
        val c3 = contactos.save(Contacto(
            organizacion = holding, nombre = "Paula Díaz (demo)", cargo = "Analista de Beneficios",
            area = AreaContacto.BENEFICIOS, correo = "paula.diaz@ejemplo.cl",
            telefono = "+56 9 0000 0003", fechaUltimoContacto = dias(-3)))
        contactos.save(Contacto(
            organizacion = plaza, nombre = "Jorge Silva (demo)", cargo = "Coordinador de Marketing",
            area = AreaContacto.MARKETING, correo = "jorge.silva@ejemplo.cl",
            telefono = "+56 9 0000 0004", fechaUltimoContacto = dias(-1)))
        val c5 = contactos.save(Contacto(
            organizacion = universidad, nombre = "Elena Mora (demo)", cargo = "Directora de Extensión",
            area = AreaContacto.ACTIVACIONES, correo = "elena.mora@ejemplo.cl",
            telefono = "+56 9 0000 0005", fechaUltimoContacto = dias(-2)))

        // ---------- Eventos ----------
        val e1 = eventos.save(Evento(
            nombre = "Feria de Bienestar Municipal", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            descripcion = "Feria anual de salud y beneficios para vecinos.",
            fechaInicio = dias(6), fechaTermino = dias(6), horario = "10:00 - 17:00",
            direccion = "Plaza de Armas (demo)", comuna = "Puente Alto", region = "Metropolitana",
            organizacion = muni, publicoEsperado = 1500, publicoObjetivo = "Familias",
            condiciones = "Stand de 3x3 m", costoReferencial = 150_000))
        val e2 = eventos.save(Evento(
            nombre = "Expo Empresas Andina", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
            descripcion = "Feria de proveedores y beneficios corporativos.",
            fechaInicio = dias(20), fechaTermino = dias(22), horario = "09:00 - 18:00",
            comuna = "Santiago", region = "Metropolitana",
            organizacion = feria, publicoEsperado = 4000, publicoObjetivo = "Empresas y trabajadores",
            costoReferencial = 400_000))
        val e3 = eventos.save(Evento(
            nombre = "Jornada de Beneficios Alfa", tipo = TipoOportunidad.JORNADA_BENEFICIOS,
            fechaInicio = dias(3), fechaTermino = dias(3), horario = "12:00 - 16:00",
            comuna = "Las Condes", region = "Metropolitana",
            organizacion = holding, publicoEsperado = 600, publicoObjetivo = "Trabajadores"))
        val e4 = eventos.save(Evento(
            nombre = "Festival de Verano Plaza", tipo = TipoOportunidad.ACTIVACION_COMERCIAL,
            fechaInicio = dias(35), fechaTermino = dias(37), horario = "11:00 - 21:00",
            comuna = "Viña del Mar", region = "Valparaíso",
            organizacion = plaza, publicoEsperado = 8000, publicoObjetivo = "Visitantes y turistas",
            costoReferencial = 600_000))
        val e5 = eventos.save(Evento(
            nombre = "Feria de Beneficios Estudiantiles", tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
            descripcion = "Feria anual de servicios para estudiantes y funcionarios.",
            fechaInicio = dias(12), fechaTermino = dias(12), horario = "10:00 - 16:00",
            comuna = "Concepción", region = "Biobío",
            organizacion = universidad, publicoEsperado = 2500, publicoObjetivo = "Estudiantes",
            condiciones = "Stand de 2x2 m", costoReferencial = 90_000))

        // ---------- Oportunidades ----------
        val o1 = oportunidades.save(Oportunidad(
            nombre = "Stand en Feria de Bienestar", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            fuente = "Sitio web municipal", evento = e1, responsable = ana, prioridad = Prioridad.ALTA,
            potencial = 4, afinidad = 5, estado = EstadoOportunidad.PROPUESTA_ENVIADA,
            fechaDeteccion = dias(-15), fechaLimitePostulacion = dias(2)))
        val o2 = oportunidades.save(Oportunidad(
            nombre = "Participación Expo Empresas", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
            fuente = "Redes sociales", evento = e2, responsable = ana, prioridad = Prioridad.ALTA,
            potencial = 5, afinidad = 4, estado = EstadoOportunidad.CONTACTO_REALIZADO,
            fechaDeteccion = dias(-12)))
        val o3 = oportunidades.save(Oportunidad(
            nombre = "Jornada interna Alfa", tipo = TipoOportunidad.JORNADA_BENEFICIOS,
            fuente = "Referencia", evento = e3, responsable = carlos, prioridad = Prioridad.MEDIA,
            potencial = 3, afinidad = 4, estado = EstadoOportunidad.CONTACTO_PENDIENTE,
            fechaDeteccion = dias(-3)))
        val o4 = oportunidades.save(Oportunidad(
            nombre = "Activación Festival de Verano", tipo = TipoOportunidad.ACTIVACION_COMERCIAL,
            fuente = "Correo", evento = e4, responsable = carlos, prioridad = Prioridad.MEDIA,
            potencial = 5, afinidad = 3, estado = EstadoOportunidad.EVENTO_IDENTIFICADO,
            fechaDeteccion = dias(-1)))
        val o5 = oportunidades.save(Oportunidad(
            nombre = "Stand en Feria Estudiantil", tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
            fuente = "Contacto directo", evento = e5, responsable = diego, prioridad = Prioridad.BAJA,
            potencial = 3, afinidad = 3, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA,
            fechaDeteccion = dias(-9), fechaLimitePostulacion = dias(8)))

        // ---------- Seguimientos (historial) ----------

        // o1 (Ana): propuesta enviada y seguimiento ATRASADO (la próxima fecha ya pasó)
        seguimientos.save(Seguimiento(
            oportunidad = o1, usuario = ana, tipoAccion = TipoAccion.INVESTIGACION_INICIAL,
            fechaHora = dias(-15), resultado = "Evento detectado en el sitio web municipal.",
            proximaAccion = "Buscar contacto en Bienestar", fechaProximoSeguimiento = dias(-13)))
        seguimientos.save(Seguimiento(
            oportunidad = o1, usuario = ana, contacto = c1, tipoAccion = TipoAccion.PRIMER_CONTACTO,
            fechaHora = dias(-10), resultado = "Marta Lagos se muestra interesada.",
            proximaAccion = "Enviar propuesta", fechaProximoSeguimiento = dias(-5),
            estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO))
        seguimientos.save(Seguimiento(
            oportunidad = o1, usuario = ana, contacto = c1, tipoAccion = TipoAccion.ENVIO_PROPUESTA,
            fechaHora = dias(-4), resultado = "Propuesta ficticia enviada.",
            proximaAccion = "Llamar para confirmar recepción", fechaProximoSeguimiento = dias(-1),
            estadoNuevo = EstadoOportunidad.PROPUESTA_ENVIADA))

        // o2 (Ana): seguimiento PRÓXIMO
        seguimientos.save(Seguimiento(
            oportunidad = o2, usuario = ana, contacto = c2, tipoAccion = TipoAccion.PRIMER_CONTACTO,
            fechaHora = dias(-8), resultado = "Interesado, pide dossier de presentación.",
            proximaAccion = "Enviar presentación", fechaProximoSeguimiento = dias(2),
            estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO))

        // o3 (Carlos): seguimiento PRÓXIMO
        seguimientos.save(Seguimiento(
            oportunidad = o3, usuario = carlos, contacto = c3, tipoAccion = TipoAccion.BUSQUEDA_CONTACTO,
            fechaHora = dias(-3), resultado = "Se identificó a la analista de beneficios.",
            proximaAccion = "Realizar primer contacto", fechaProximoSeguimiento = dias(1)))

        // o4 (Carlos): sin contacto todavía (solo investigación, sin próxima fecha)
        seguimientos.save(Seguimiento(
            oportunidad = o4, usuario = carlos, tipoAccion = TipoAccion.INVESTIGACION_INICIAL,
            fechaHora = dias(-1), resultado = "Evento detectado por correo. Falta buscar contacto."))

        // o5 (Diego): propuesta sin respuesta
        seguimientos.save(Seguimiento(
            oportunidad = o5, usuario = diego, contacto = c5, tipoAccion = TipoAccion.ENVIO_PROPUESTA,
            fechaHora = dias(-6), resultado = "Propuesta ficticia enviada.",
            proximaAccion = "Consultar respuesta", fechaProximoSeguimiento = dias(-2),
            estadoNuevo = EstadoOportunidad.EN_ESPERA_RESPUESTA))
        seguimientos.save(Seguimiento(
            oportunidad = o5, usuario = diego, contacto = c5, tipoAccion = TipoAccion.LLAMADA,
            fechaHora = dias(-2), resultado = "Sin respuesta aún; piden esperar unos días.",
            proximaAccion = "Volver a llamar", fechaProximoSeguimiento = dias(3)))
    }
}