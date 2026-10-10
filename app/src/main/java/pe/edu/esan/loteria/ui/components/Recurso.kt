package pe.edu.esan.loteria.ui.components

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** Estado de carga de datos que llegan desde Firestore. */
sealed interface Recurso<out T> {
    data object Cargando : Recurso<Nothing>
    data class Listo<T>(val dato: T) : Recurso<T>
    data class Error(val mensaje: String) : Recurso<Nothing>
}

fun <T> Flow<T>.comoRecurso(): Flow<Recurso<T>> =
    map<T, Recurso<T>> { Recurso.Listo(it) }
        .onStart { emit(Recurso.Cargando) }
        .catch { e -> emit(Recurso.Error(e.localizedMessage ?: "Error al leer Firestore.")) }
