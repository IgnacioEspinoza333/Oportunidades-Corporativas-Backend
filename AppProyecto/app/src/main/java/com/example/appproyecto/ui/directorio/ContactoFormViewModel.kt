package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appproyecto.AppProyectoApp
import com.example.appproyecto.data.model.AreaContacto
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.remote.Respuesta
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import com.example.appproyecto.util.validarCorreoContacto
import com.example.appproyecto.util.validarTelefonoOpcional
import com.example.appproyecto.util.validarTextoObligatorio
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val MEDIOS_PREFERENTES = listOf("Correo", "Teléfono", "WhatsApp", "Reunión presencial")
val ESTADOS_CONTACTO = listOf("Activo", "Inactivo")

data class ContactoFormUiState(
    val organizacionNombre: String = "",
    val nombre: String = "",
    val cargo: String = "",
    val area: String = AreaContacto.EVENTOS.name,
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: String = MEDIOS_PREFERENTES.first(),
    val estado: String = ESTADOS_CONTACTO.first(),
    val observaciones: String = "",
    val esEdicion: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val errorNombre: String? = null,
    val errorCargo: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorObservaciones: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
)

class ContactoFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ContactoRepository,
    private val organizaciones: OrganizacionRepository
) : ViewModel() {

    private val organizacionId: Long = savedStateHandle["orgId"] ?: 0L

    /** 0 = contacto nuevo. */
    private val contactoId: Long = savedStateHandle["id"] ?: 0L

    private val _uiState = MutableStateFlow(ContactoFormUiState(esEdicion = contactoId != 0L))
    val uiState: StateFlow<ContactoFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1) nombre de la organización para el título
            when (val org = organizaciones.obtener(organizacionId)) {
                is Respuesta.Exito -> _uiState.update { it.copy(organizacionNombre = org.datos.nombre) }
                is Respuesta.Error -> {
                    _uiState.update { it.copy(cargando = false, errorGeneral = org.mensaje) }
                    return@launch
                }
            }
            // 2) si es edición, se cargan los datos del contacto
            if (contactoId != 0L) {
                when (val r = repository.obtener(contactoId)) {
                    is Respuesta.Exito -> {
                        val c = r.datos
                        _uiState.update {
                            it.copy(
                                nombre = c.nombre, cargo = c.cargo, area = c.area, correo = c.correo,
                                telefono = c.telefono, medioPreferente = c.medioPreferente, estado = c.estado,
                                observaciones = c.observaciones
                            )
                        }
                    }
                    is Respuesta.Error -> {
                        _uiState.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                        return@launch
                    }
                }
            }
            _uiState.update { it.copy(cargando = false) }
        }
    }

    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun cambiarCargo(v: String) = _uiState.update { it.copy(cargo = v, errorCargo = null) }
    fun cambiarArea(v: String) = _uiState.update { it.copy(area = v) }
    fun cambiarCorreo(v: String) = _uiState.update { it.copy(correo = v, errorCorreo = null, errorGeneral = null) }
    fun cambiarTelefono(v: String) = _uiState.update { it.copy(telefono = v, errorTelefono = null) }
    fun cambiarMedio(v: String) = _uiState.update { it.copy(medioPreferente = v) }
    fun cambiarEstado(v: String) = _uiState.update { it.copy(estado = v) }
    fun cambiarObservaciones(v: String) = _uiState.update { it.copy(observaciones = v, errorObservaciones = null) }

    fun guardar() {
        val s = _uiState.value
        if (s.guardando || s.cargando) return

        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 3, 80)
        val errCargo = validarTextoObligatorio(s.cargo, "El cargo", 3, 60)
        val errCorreo = validarCorreoContacto(s.correo)
        val errTelefono = validarTelefonoOpcional(s.telefono)
        val errObs = validarTextoOpcional(s.observaciones, "Las observaciones", 300)

        if (listOf(errNombre, errCargo, errCorreo, errTelefono, errObs).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorCargo = errCargo, errorCorreo = errCorreo,
                    errorTelefono = errTelefono, errorObservaciones = errObs
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val contacto = Contacto(
                id = contactoId,
                organizacionId = organizacionId,
                nombre = s.nombre.trim(),
                cargo = s.cargo.trim(),
                area = s.area,
                correo = s.correo.trim(),
                telefono = s.telefono.trim(),
                medioPreferente = s.medioPreferente,
                estado = s.estado,
                observaciones = s.observaciones.trim()
            )
            val resultado = if (s.esEdicion) repository.actualizar(contacto)
            else repository.crear(contacto)
            when (resultado) {
                is Respuesta.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                is Respuesta.Error -> _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as AppProyectoApp).contenedor
                ContactoFormViewModel(createSavedStateHandle(), c.contactoRepository, c.organizacionRepository)
            }
        }
    }
}