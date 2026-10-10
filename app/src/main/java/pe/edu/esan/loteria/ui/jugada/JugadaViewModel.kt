package pe.edu.esan.loteria.ui.jugada

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.data.model.Sorteo
import pe.edu.esan.loteria.data.repository.AuthRepository
import pe.edu.esan.loteria.data.repository.JugadaRechazadaException
import pe.edu.esan.loteria.data.repository.JugadaRepository
import pe.edu.esan.loteria.data.repository.SorteoRepository
import pe.edu.esan.loteria.domain.ValidadorJugada
import pe.edu.esan.loteria.ui.components.Recurso
import pe.edu.esan.loteria.ui.components.comoRecurso
import pe.edu.esan.loteria.ui.navigation.Rutas

data class JugadaUiState(
    val campos: List<String> = List(ValidadorJugada.CANTIDAD) { "" },
    val jugadaGuardada: List<Int>? = null,
    val cargandoJugada: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class JugadaViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val authRepository = AuthRepository()
    private val sorteoRepository = SorteoRepository()
    private val jugadaRepository = JugadaRepository()

    val sorteoId: String = checkNotNull(savedStateHandle[Rutas.ARG_SORTEO_ID])

    /** Estado del sorteo en tiempo real: si pasa a "en_curso" se bloquea el formulario. */
    val sorteo: StateFlow<Recurso<Sorteo?>> = sorteoRepository.escucharSorteo(sorteoId)
        .comoRecurso()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Recurso.Cargando)

    var uiState by mutableStateOf(JugadaUiState())
        private set

    init {
        cargarJugada()
    }

    /** Recupera desde Firestore la jugada que el usuario ya registró (si existe). */
    private fun cargarJugada() {
        val uid = authRepository.usuarioActual?.uid
        if (uid == null) {
            uiState = uiState.copy(cargandoJugada = false, error = "La sesión expiró. Vuelve a iniciar sesión.")
            return
        }
        viewModelScope.launch {
            try {
                val jugada = jugadaRepository.obtenerJugada(sorteoId, uid)
                uiState = uiState.copy(
                    cargandoJugada = false,
                    jugadaGuardada = jugada?.numeros,
                    campos = jugada?.numeros?.map { it.toString() } ?: uiState.campos
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(
                    cargandoJugada = false,
                    error = "No se pudo recuperar tu jugada: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onCampoChange(indice: Int, valor: String) {
        // Solo dígitos, máximo 2 (los números van de 1 a 45).
        val filtrado = valor.filter { it.isDigit() }.take(2)
        val nuevos = uiState.campos.toMutableList().also { it[indice] = filtrado }
        uiState = uiState.copy(campos = nuevos, error = null, mensaje = null)
    }

    fun guardar(estadoActual: EstadoSorteo) {
        if (uiState.guardando) return
        if (estadoActual != EstadoSorteo.ABIERTO) {
            uiState = uiState.copy(error = "El sorteo ya inició: no se pueden registrar ni modificar jugadas.")
            return
        }
        val numeros = when (val r = ValidadorJugada.validar(uiState.campos)) {
            is ValidadorJugada.Resultado.Invalida -> {
                uiState = uiState.copy(error = r.mensaje, mensaje = null)
                return
            }
            is ValidadorJugada.Resultado.Valida -> r.numeros
        }
        val usuario = authRepository.usuarioActual
        if (usuario == null) {
            uiState = uiState.copy(error = "La sesión expiró. Vuelve a iniciar sesión.")
            return
        }

        val eraNueva = uiState.jugadaGuardada == null
        uiState = uiState.copy(guardando = true, error = null, mensaje = null)
        viewModelScope.launch {
            try {
                jugadaRepository.guardarJugada(sorteoId, usuario.uid, usuario.email, numeros)
                uiState = uiState.copy(
                    guardando = false,
                    jugadaGuardada = numeros,
                    campos = numeros.map { it.toString() },
                    mensaje = if (eraNueva) "¡Jugada registrada!" else "Jugada actualizada."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(guardando = false, error = mensajeError(e))
            }
        }
    }

    private fun mensajeError(e: Throwable): String {
        val cadena = generateSequence(e) { it.cause }
        cadena.filterIsInstance<JugadaRechazadaException>().firstOrNull()?.let {
            return it.message ?: "No se pudo registrar la jugada."
        }
        if (cadena.any { it is FirebaseNetworkException }) {
            return "Sin conexión a internet. Inténtalo nuevamente."
        }
        val firestore = cadena.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        return when (firestore?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Firestore rechazó la jugada: el sorteo ya no está abierto o los datos no son válidos."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Firestore no está disponible. Revisa tu conexión."
            else -> e.localizedMessage ?: "No se pudo registrar la jugada."
        }
    }
}
