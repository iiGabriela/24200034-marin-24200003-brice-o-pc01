package pe.edu.esan.loteria.ui.seguimiento

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import pe.edu.esan.loteria.data.model.Bolilla
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.data.model.Jugada
import pe.edu.esan.loteria.data.model.Sorteo
import pe.edu.esan.loteria.data.repository.AuthRepository
import pe.edu.esan.loteria.data.repository.JugadaRepository
import pe.edu.esan.loteria.data.repository.SorteoRepository
import pe.edu.esan.loteria.domain.CalculoSorteo
import pe.edu.esan.loteria.domain.ResultadoFinal
import pe.edu.esan.loteria.ui.navigation.Rutas

data class SeguimientoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val sorteo: Sorteo? = null,
    val bolillas: List<Bolilla> = emptyList(),
    val jugada: List<Int>? = null,
    val numerosAcertados: Set<Int> = emptySet(),
    val bolillasPublicadas: Int = 0,
    val aciertos: Int = 0,
    val mensajeEstado: String = "",
    val resultado: ResultadoFinal? = null
)

/**
 * Escucha EN TIEMPO REAL tres fuentes de Firestore y recalcula todo en cada cambio:
 *   1. sorteos/{id}             -> estado del sorteo
 *   2. sorteos/{id}/bolillas    -> bolillas publicadas
 *   3. sorteos/{id}/jugadas/uid -> jugada del usuario de la sesión
 *
 * combine() junta los tres flujos: cada vez que cualquiera emite (p. ej. se publica una
 * bolilla desde la consola), se vuelven a calcular aciertos, bolillas publicadas y resultado.
 *
 * stateIn(WhileSubscribed(5_000)) comparte UNA sola suscripción (no hay listeners duplicados)
 * y la cancela 5 s después de que la pantalla deja de observar; en ese momento cada
 * callbackFlow ejecuta awaitClose { registro.remove() } y los listeners se eliminan.
 * Los 5 s evitan reiniciar los listeners al rotar la pantalla.
 */
class SeguimientoViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val authRepository = AuthRepository()
    private val sorteoRepository = SorteoRepository()
    private val jugadaRepository = JugadaRepository()

    private val sorteoId: String = checkNotNull(savedStateHandle[Rutas.ARG_SORTEO_ID])

    val uiState: StateFlow<SeguimientoUiState> = run {
        val uid = authRepository.usuarioActual?.uid
        val flujoJugada = if (uid != null) {
            jugadaRepository.escucharJugada(sorteoId, uid)
        } else {
            flowOf<Jugada?>(null)
        }
        combine(
            sorteoRepository.escucharSorteo(sorteoId),
            sorteoRepository.escucharBolillas(sorteoId),
            flujoJugada
        ) { sorteo, bolillas, jugada ->
            calcularEstado(sorteo, bolillas, jugada)
        }
            .catch { e ->
                emit(
                    SeguimientoUiState(
                        cargando = false,
                        error = e.localizedMessage ?: "Error al escuchar el sorteo."
                    )
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeguimientoUiState())
    }

    /** Aquí se aplican los cálculos (ver domain/CalculoSorteo.kt). */
    private fun calcularEstado(
        sorteo: Sorteo?,
        bolillas: List<Bolilla>,
        jugada: Jugada?
    ): SeguimientoUiState {
        val estado = sorteo?.estado ?: EstadoSorteo.DESCONOCIDO
        val numeros = jugada?.numeros.orEmpty()
        return SeguimientoUiState(
            cargando = false,
            sorteo = sorteo,
            bolillas = bolillas,
            jugada = jugada?.numeros,
            numerosAcertados = CalculoSorteo.numerosAcertados(numeros, bolillas),
            bolillasPublicadas = CalculoSorteo.bolillasPublicadas(bolillas),
            aciertos = CalculoSorteo.aciertos(numeros, bolillas),
            mensajeEstado = CalculoSorteo.mensajeEstado(estado, bolillas),
            resultado = if (jugada != null) {
                CalculoSorteo.resultadoFinal(estado, numeros, bolillas)
            } else {
                null
            }
        )
    }
}
