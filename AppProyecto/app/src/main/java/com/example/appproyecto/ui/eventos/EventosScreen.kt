package com.example.appproyecto.ui.eventos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.TipoOportunidad
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(viewModel: EventosViewModel = viewModel(factory = EventosViewModel.Factory)) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // Cada vez que la pantalla vuelve a mostrarse (por ejemplo, tras crear un evento) se recarga
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Eventos") })

        when {
            estado.cargando -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }

            estado.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(estado.error ?: "", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = viewModel::reintentar, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Reintentar")
                }
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.eventos, key = { it.id }) { evento ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (evento.fotoUri != null) {
                                FotoEvento(
                                    ruta = evento.fotoUri,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(evento.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("${evento.tipo.aEnum(TipoOportunidad.OTRA).etiqueta} · ${formatearFecha(evento.fechaInicio)}")
                                Text("${evento.comuna} · ${evento.publicoEsperado} personas esperadas")
                            }
                        }
                    }
                }
            }
        }
    }
}