package pe.edu.esan.loteria.ui.jugada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.data.model.Sorteo
import pe.edu.esan.loteria.ui.components.BolillaCirculo
import pe.edu.esan.loteria.ui.components.EstadoChip
import pe.edu.esan.loteria.ui.components.LoteriaTopBar
import pe.edu.esan.loteria.ui.components.Recurso
import pe.edu.esan.loteria.ui.theme.VerdeAcierto

@Composable
fun JugadaScreen(
    correo: String?,
    onVolver: () -> Unit,
    onVerSeguimiento: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: JugadaViewModel = viewModel()
) {
    val recursoSorteo by viewModel.sorteo.collectAsStateWithLifecycle()
    val estado = viewModel.uiState

    Scaffold(
        topBar = {
            LoteriaTopBar(
                titulo = "Registrar jugada",
                correo = correo,
                onVolver = onVolver,
                onCerrarSesion = onCerrarSesion
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val r = recursoSorteo) {
                Recurso.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is Recurso.Error -> Text(
                    "No se pudo leer el sorteo: ${r.mensaje}",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )
                is Recurso.Listo -> {
                    val sorteo = r.dato
                    if (sorteo == null) {
                        Text(
                            "El sorteo no existe.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ContenidoJugada(
                            sorteo = sorteo,
                            estado = estado,
                            onCampoChange = viewModel::onCampoChange,
                            onGuardar = { viewModel.guardar(sorteo.estado) },
                            onVerSeguimiento = { onVerSeguimiento(sorteo.id) },
                            onVolver = onVolver
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenidoJugada(
    sorteo: Sorteo,
    estado: JugadaUiState,
    onCampoChange: (Int, String) -> Unit,
    onGuardar: () -> Unit,
    onVerSeguimiento: () -> Unit,
    onVolver: () -> Unit
) {
    val abierto = sorteo.estado == EstadoSorteo.ABIERTO
    val editable = abierto && !estado.guardando && !estado.cargandoJugada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera del sorteo: nombre, fecha y estado
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        sorteo.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    EstadoChip(sorteo.estado)
                }
                Text("Fecha: ${sorteo.fecha}")
            }
        }

        // Jugada ya registrada
        if (estado.jugadaGuardada != null) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tu jugada registrada", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        estado.jugadaGuardada.forEach { n ->
                            BolillaCirculo(numero = n, resaltada = false, tamano = 42.dp)
                        }
                    }
                }
            }
        }

        if (estado.cargandoJugada) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (abierto) {
            Text(
                if (estado.jugadaGuardada == null)
                    "Ingresa 6 números distintos entre 1 y 45:"
                else
                    "Puedes modificar tu jugada mientras el sorteo siga abierto:",
                style = MaterialTheme.typography.bodyLarge
            )
            // 6 campos en 2 filas de 3
            for (fila in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (columna in 0 until 3) {
                        val indice = fila * 3 + columna
                        OutlinedTextField(
                            value = estado.campos[indice],
                            onValueChange = { onCampoChange(indice, it) },
                            label = { Text("N° ${indice + 1}") },
                            singleLine = true,
                            enabled = editable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (indice == 5) ImeAction.Done else ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Button(
                onClick = onGuardar,
                enabled = editable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (estado.jugadaGuardada == null) "Registrar jugada" else "Actualizar jugada")
                }
            }
        } else {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = if (estado.jugadaGuardada == null)
                        "El sorteo ya inició. Ya no se pueden registrar jugadas."
                    else
                        "El sorteo ya inició. Tu jugada no puede modificarse.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (estado.error != null) {
            Text(estado.error, color = MaterialTheme.colorScheme.error)
        }
        if (estado.mensaje != null) {
            Text(estado.mensaje, color = VerdeAcierto, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onVerSeguimiento,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver seguimiento en vivo →")
        }
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a sorteos")
        }
    }
}
