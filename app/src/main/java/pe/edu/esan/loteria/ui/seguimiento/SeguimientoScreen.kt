package pe.edu.esan.loteria.ui.seguimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.domain.CalculoSorteo
import pe.edu.esan.loteria.domain.ResultadoFinal
import pe.edu.esan.loteria.ui.components.BolillaCirculo
import pe.edu.esan.loteria.ui.components.EstadoChip
import pe.edu.esan.loteria.ui.components.LoteriaTopBar
import pe.edu.esan.loteria.ui.theme.NaranjaAviso
import pe.edu.esan.loteria.ui.theme.NaranjaAvisoClaro
import pe.edu.esan.loteria.ui.theme.VerdeAcierto
import pe.edu.esan.loteria.ui.theme.VerdeAciertoClaro

@Composable
fun SeguimientoScreen(
    correo: String?,
    onVolver: () -> Unit,
    onIrASorteos: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: SeguimientoViewModel = viewModel()
) {
    // collectAsStateWithLifecycle: deja de observar cuando la pantalla no está visible.
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LoteriaTopBar(
                titulo = "Seguimiento en vivo",
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
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                estado.error != null -> Text(
                    "No se pudo escuchar el sorteo:\n${estado.error}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                estado.sorteo == null -> Text(
                    "El sorteo no existe.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> ContenidoSeguimiento(estado, onVolver, onIrASorteos)
            }
        }
    }
}

@Composable
private fun ContenidoSeguimiento(
    estado: SeguimientoUiState,
    onVolver: () -> Unit,
    onIrASorteos: () -> Unit
) {
    val sorteo = estado.sorteo ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabecera
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

        // 2. Mensaje según estado
        val enCurso = sorteo.estado == EstadoSorteo.EN_CURSO
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (enCurso) NaranjaAvisoClaro else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                estado.mensajeEstado,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enCurso) NaranjaAviso else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 3. Bolillas en orden de salida (los espacios vacíos son las que faltan)
        Text(
            "Bolillas publicadas: ${estado.bolillasPublicadas} / ${CalculoSorteo.TOTAL_BOLILLAS}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (posicion in 1..CalculoSorteo.TOTAL_BOLILLAS) {
                val bolilla = estado.bolillas.firstOrNull { it.orden == posicion }
                BolillaCirculo(
                    numero = bolilla?.numero,
                    resaltada = bolilla != null && bolilla.numero in estado.numerosAcertados,
                    tamano = 46.dp,
                    etiqueta = "${posicion}°"
                )
            }
        }

        // 4. Jugada del usuario con los aciertos resaltados
        Text(
            "Tu jugada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (estado.jugada == null) {
            Text(
                "No registraste una jugada para este sorteo.",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                estado.jugada.forEach { numero ->
                    val acertado = numero in estado.numerosAcertados
                    BolillaCirculo(
                        numero = numero,
                        resaltada = acertado,
                        tamano = 46.dp,
                        etiqueta = if (acertado) "✓" else " "
                    )
                }
            }
            Text(
                "Aciertos: ${estado.aciertos}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = VerdeAcierto
            )
        }

        // 5. Resultado final (solo si FINALIZADO y 6 bolillas)
        if (estado.jugada != null) {
            TarjetaResultado(estado.resultado, sorteo.estado)
        }

        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("← Volver a mi jugada")
        }
        OutlinedButton(onClick = onIrASorteos, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a la lista de sorteos")
        }
    }
}

@Composable
private fun TarjetaResultado(resultado: ResultadoFinal?, estadoSorteo: EstadoSorteo) {
    val (fondo, colorTexto, texto) = when (resultado) {
        ResultadoFinal.GANADOR ->
            Triple(VerdeAciertoClaro, VerdeAcierto, ResultadoFinal.GANADOR.texto)
        ResultadoFinal.SIGUE_INTENTANDO ->
            Triple(NaranjaAvisoClaro, NaranjaAviso, ResultadoFinal.SIGUE_INTENTANDO.texto)
        null -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            if (estadoSorteo == EstadoSorteo.ABIERTO) "El resultado aparecerá al finalizar el sorteo."
            else "Esperando resultados…"
        )
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (resultado == null) MaterialTheme.colorScheme.surfaceVariant else fondo
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Resultado final", style = MaterialTheme.typography.labelLarge)
            Text(
                texto,
                color = colorTexto,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (resultado == null) 18.sp else 30.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
