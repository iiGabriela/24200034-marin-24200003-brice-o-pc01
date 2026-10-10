package pe.edu.esan.loteria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.ui.theme.NaranjaAviso
import pe.edu.esan.loteria.ui.theme.VerdeAcierto

/** Barra superior con el correo del usuario autenticado, botón volver y cerrar sesión. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoteriaTopBar(
    titulo: String,
    correo: String?,
    onVolver: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (correo != null) {
                    Text(
                        text = correo,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            if (onCerrarSesion != null) {
                IconButton(onClick = onCerrarSesion) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Círculo con un número.
 * - numero = null dibuja un espacio vacío ("?") para una bolilla aún no publicada.
 * - resaltada = true pinta el círculo de verde (número acertado).
 */
@Composable
fun BolillaCirculo(
    numero: Int?,
    resaltada: Boolean,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
    etiqueta: String? = null
) {
    val fondo = when {
        numero == null -> MaterialTheme.colorScheme.surfaceVariant
        resaltada -> VerdeAcierto
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val texto = when {
        numero == null -> MaterialTheme.colorScheme.onSurfaceVariant
        resaltada -> Color.White
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(tamano)
                .background(fondo, CircleShape)
                .border(
                    width = if (resaltada) 3.dp else 1.dp,
                    color = if (resaltada) VerdeAcierto else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero?.toString() ?: "?",
                color = texto,
                fontWeight = FontWeight.Bold,
                fontSize = (tamano.value * 0.38f).sp
            )
        }
        if (etiqueta != null) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Etiqueta de color para el estado del sorteo. */
@Composable
fun EstadoChip(estado: EstadoSorteo, modifier: Modifier = Modifier) {
    val color = when (estado) {
        EstadoSorteo.ABIERTO -> VerdeAcierto
        EstadoSorteo.EN_CURSO -> NaranjaAviso
        EstadoSorteo.FINALIZADO -> MaterialTheme.colorScheme.primary
        EstadoSorteo.DESCONOCIDO -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = modifier
            .border(1.dp, color, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado.etiqueta,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
