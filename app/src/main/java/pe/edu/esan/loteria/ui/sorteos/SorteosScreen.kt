package pe.edu.esan.loteria.ui.sorteos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.data.model.Sorteo
import pe.edu.esan.loteria.ui.components.EstadoChip
import pe.edu.esan.loteria.ui.components.LoteriaTopBar
import pe.edu.esan.loteria.ui.components.Recurso

@Composable
fun SorteosScreen(
    correo: String?,
    onSeleccionarSorteo: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: SorteosViewModel = viewModel()
) {
    val recurso by viewModel.sorteos.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LoteriaTopBar(
                titulo = "Sorteos disponibles",
                correo = correo,
                onCerrarSesion = onCerrarSesion
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val r = recurso) {
                Recurso.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is Recurso.Error -> MensajeCentrado("No se pudieron cargar los sorteos:\n${r.mensaje}")
                is Recurso.Listo -> {
                    if (r.dato.isEmpty()) {
                        MensajeCentrado(
                            "Aún no hay sorteos.\nCrea el documento del sorteo en la colección " +
                                "\"sorteos\" desde la consola de Firebase."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "Elige un sorteo para registrar tu jugada o seguirlo en vivo.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            items(r.dato, key = { it.id }) { sorteo ->
                                TarjetaSorteo(sorteo, onClick = { onSeleccionarSorteo(sorteo.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaSorteo(sorteo: Sorteo, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    sorteo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(sorteo.estado)
            }
            Spacer(Modifier.height(4.dp))
            Text("Fecha: ${sorteo.fecha}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Toca para continuar →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MensajeCentrado(texto: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center)
    }
}
