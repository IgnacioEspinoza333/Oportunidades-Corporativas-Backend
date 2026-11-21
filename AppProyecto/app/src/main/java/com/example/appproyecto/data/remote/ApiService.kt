package com.example.appproyecto.data.remote

import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.SeguimientoPendiente
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Cada función corresponde a un endpoint de nuestro backend Spring Boot.
 * Las rutas son relativas a ApiConfig.URL_BACKEND. El encabezado X-Usuario-Id
 * lo agrega ApiClient automáticamente.
 */
interface ApiService {

    // ---------- Acceso ----------

    @POST("api/auth/login")
    suspend fun login(@Body solicitud: LoginRequest): UsuarioDto

    @GET("api/auth/me")
    suspend fun usuarioActual(): UsuarioDto

    @GET("api/usuarios/ejecutivos")
    suspend fun ejecutivos(): List<UsuarioDto>

    // ---------- Organizaciones ----------

    @GET("api/organizaciones")
    suspend fun organizaciones(): List<OrganizacionDto>

    @GET("api/organizaciones/{id}")
    suspend fun organizacion(@Path("id") id: Long): OrganizacionDto

    @POST("api/organizaciones")
    suspend fun crearOrganizacion(@Body solicitud: OrganizacionRequest): OrganizacionDto

    @PUT("api/organizaciones/{id}")
    suspend fun actualizarOrganizacion(
        @Path("id") id: Long,
        @Body solicitud: OrganizacionRequest
    ): OrganizacionDto

    @DELETE("api/organizaciones/{id}")
    suspend fun eliminarOrganizacion(@Path("id") id: Long)

    // ---------- Contactos ----------

    @GET("api/organizaciones/{organizacionId}/contactos")
    suspend fun contactosDeOrganizacion(@Path("organizacionId") organizacionId: Long): List<ContactoDto>

    @POST("api/organizaciones/{organizacionId}/contactos")
    suspend fun crearContacto(
        @Path("organizacionId") organizacionId: Long,
        @Body solicitud: ContactoRequest
    ): ContactoDto

    @GET("api/contactos/{id}")
    suspend fun contacto(@Path("id") id: Long): ContactoDto

    @PUT("api/contactos/{id}")
    suspend fun actualizarContacto(
        @Path("id") id: Long,
        @Body solicitud: ContactoRequest
    ): ContactoDto

    @DELETE("api/contactos/{id}")
    suspend fun eliminarContacto(@Path("id") id: Long)

    // ---------- Eventos ----------

    @GET("api/eventos")
    suspend fun eventos(): List<EventoDto>

    @GET("api/eventos/{id}")
    suspend fun evento(@Path("id") id: Long): EventoDto

    @POST("api/eventos")
    suspend fun crearEvento(@Body solicitud: EventoRequest): EventoDto

    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Path("id") id: Long,
        @Body solicitud: EventoRequest
    ): EventoDto

    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(@Path("id") id: Long)

    // ---------- Oportunidades ----------

    @GET("api/oportunidades")
    suspend fun oportunidades(): List<OportunidadItem>

    @GET("api/oportunidades/{id}")
    suspend fun oportunidad(@Path("id") id: Long): OportunidadDetalle

    @POST("api/oportunidades")
    suspend fun crearOportunidad(@Body solicitud: OportunidadRequest): OportunidadDetalle

    @PUT("api/oportunidades/{id}")
    suspend fun actualizarOportunidad(
        @Path("id") id: Long,
        @Body solicitud: OportunidadRequest
    ): OportunidadDetalle

    @PATCH("api/oportunidades/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body solicitud: CambioEstadoRequest
    ): OportunidadDetalle

    @PATCH("api/oportunidades/{id}/responsable")
    suspend fun asignarResponsable(
        @Path("id") id: Long,
        @Body solicitud: AsignarRequest
    ): OportunidadDetalle

    // ---------- Seguimientos ----------

    @GET("api/oportunidades/{id}/seguimientos")
    suspend fun historial(@Path("id") oportunidadId: Long): List<SeguimientoItem>

    @POST("api/oportunidades/{id}/seguimientos")
    suspend fun registrarSeguimiento(
        @Path("id") oportunidadId: Long,
        @Body solicitud: SeguimientoRequest
    ): SeguimientoItem

    @GET("api/seguimientos/pendientes")
    suspend fun seguimientosPendientes(): List<SeguimientoPendiente>
}