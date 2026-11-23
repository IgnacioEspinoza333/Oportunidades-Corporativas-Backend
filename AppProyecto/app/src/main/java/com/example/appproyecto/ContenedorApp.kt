package com.example.appproyecto

import android.app.Application
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.remote.ApiClient
import com.example.appproyecto.data.remote.ApiService
import com.example.appproyecto.data.remote.FeriadosClient
import com.example.appproyecto.data.remote.FeriadosService
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.EventoRepository
import com.example.appproyecto.data.repository.EvidenciaRepository
import com.example.appproyecto.data.repository.FeriadoRepository
import com.example.appproyecto.data.repository.OportunidadApiRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.FotosEventos
import com.example.appproyecto.util.SesionManager

/**
 * Contenedor de dependencias: crea UNA sola vez los objetos que comparten las pantallas
 * y se los entrega a los ViewModels. Así ningún ViewModel arma sus propias herramientas.
 */
class ContenedorApp(application: Application) {

    // Recuerda qué usuario inició sesión
    val sesion = SesionManager(application)

    // Base de datos local (Room): solo guarda las evidencias fotográficas
    val baseDatos: AppDatabase by lazy { AppDatabase.getInstance(application) }

    // Servicio de Retrofit para hablar con el backend
    val api: ApiService by lazy { ApiClient.crearServicio(sesion) }

    // Servicio de Retrofit para la API externa de feriados
    val feriadosApi: FeriadosService by lazy { FeriadosClient.crearServicio() }

    // Fotos de eventos guardadas en el teléfono
    val fotosEventos: FotosEventos by lazy { FotosEventos(application) }

    // ---------- Repositorios ----------
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepository(api) }
    val organizacionRepository: OrganizacionRepository by lazy { OrganizacionRepository(api) }
    val contactoRepository: ContactoRepository by lazy { ContactoRepository(api) }
    val eventoRepository: EventoRepository by lazy { EventoRepository(api, fotosEventos) }
    val oportunidadRepository: OportunidadApiRepository by lazy { OportunidadApiRepository(api) }
    val evidenciaRepository: EvidenciaRepository by lazy {
        EvidenciaRepository(baseDatos.evidenciaDao(), oportunidadRepository)
    }
    val feriadoRepository: FeriadoRepository by lazy { FeriadoRepository(feriadosApi) }
}