package pe.edu.esan.loteria.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import pe.edu.esan.loteria.data.model.Bolilla
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.data.model.Jugada
import pe.edu.esan.loteria.data.model.Sorteo
import java.text.SimpleDateFormat
import java.util.Locale

/** Nombres de colecciones y campos usados en Firestore. */
object FirestoreRutas {
    const val SORTEOS = "sorteos"
    const val BOLILLAS = "bolillas"
    const val JUGADAS = "jugadas"
}

/**
 * Convierte un valor leído de Firestore a Int. Firestore devuelve los enteros como Long;
 * si desde la consola se escribió un número decimal (5.0) o un texto ("5") también se acepta.
 */
internal fun aEntero(valor: Any?): Int? = when (valor) {
    is Long -> valor.toInt()
    is Int -> valor
    is Double -> if (valor % 1.0 == 0.0) valor.toInt() else null
    is Number -> valor.toInt()
    is String -> valor.trim().toIntOrNull()
    else -> null
}

private fun leerFecha(valor: Any?): String = when (valor) {
    null -> "Sin fecha"
    is Timestamp -> SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-PE"))
        .format(valor.toDate())
    else -> valor.toString()
}

internal fun DocumentSnapshot.aSorteo(): Sorteo? {
    if (!exists()) return null
    return Sorteo(
        id = id,
        nombre = get("nombre")?.toString() ?: id,
        fecha = leerFecha(get("fecha")),
        estado = EstadoSorteo.desde(get("estado")?.toString())
    )
}

/** Devuelve null si el documento no tiene un número u orden válidos. */
internal fun DocumentSnapshot.aBolilla(sorteoId: String): Bolilla? {
    val numero = aEntero(get("numero")) ?: return null
    val orden = aEntero(get("orden")) ?: return null
    return Bolilla(id = id, sorteoId = sorteoId, numero = numero, orden = orden)
}

internal fun DocumentSnapshot.aJugada(): Jugada? {
    if (!exists()) return null
    val numeros = (get("numeros") as? List<*>)?.mapNotNull { aEntero(it) } ?: return null
    return Jugada(
        sorteoId = get("sorteoId")?.toString() ?: reference.parent.parent?.id.orEmpty(),
        uid = get("uid")?.toString() ?: id,
        numeros = numeros
    )
}
