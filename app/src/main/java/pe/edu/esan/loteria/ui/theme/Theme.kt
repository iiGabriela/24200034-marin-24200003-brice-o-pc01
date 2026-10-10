package pe.edu.esan.loteria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RojoEsan = Color(0xFFC8102E)
val RojoEsanClaro = Color(0xFFFFB3B3)
val VerdeAcierto = Color(0xFF2E7D32)
val VerdeAciertoClaro = Color(0xFFC8E6C9)
val NaranjaAviso = Color(0xFFE65100)
val NaranjaAvisoClaro = Color(0xFFFFE0B2)

private val EsquemaClaro = lightColorScheme(
    primary = RojoEsan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF410006),
    secondary = Color(0xFF775654),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF)
)

private val EsquemaOscuro = darkColorScheme(
    primary = RojoEsanClaro,
    onPrimary = Color(0xFF68000F),
    primaryContainer = Color(0xFF93001E),
    onPrimaryContainer = Color(0xFFFFDAD8),
    secondary = Color(0xFFE7BDB9)
)

@Composable
fun LoteriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        content = content
    )
}
