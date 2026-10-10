package pe.edu.esan.loteria.domain

import pe.edu.esan.loteria.data.model.Bolilla
import pe.edu.esan.loteria.data.model.EstadoSorteo

/** Texto que la APP determina; nunca se guarda en Firestore. */
enum class ResultadoFinal(val texto: String) {
    GANADOR("GANADOR"),
    SIGUE_INTENTANDO("SIGUE INTENTANDO")
}

/**
 * Cálculos del seguimiento. Todo se calcula en la app a partir de la jugada almacenada
 * y de las bolillas recibidas desde Firestore.
 */
object CalculoSorteo {
    const val TOTAL_BOLILLAS = 6

    /** Bolillas publicadas: cantidad recibida del sorteo, de 0 a 6. */
    fun bolillasPublicadas(bolillas: List<Bolilla>): Int =
        bolillas.size.coerceIn(0, TOTAL_BOLILLAS)

    /** Números de la jugada que aparecen entre las bolillas (el orden no importa). */
    fun numerosAcertados(jugada: List<Int>, bolillas: List<Bolilla>): Set<Int> =
        jugada.toSet() intersect bolillas.map { it.numero }.toSet()

    /** Aciertos: cantidad de números DISTINTOS de la jugada presentes en las bolillas. */
    fun aciertos(jugada: List<Int>, bolillas: List<Bolilla>): Int =
        numerosAcertados(jugada, bolillas).size

    /**
     * El resultado definitivo solo existe si el sorteo está FINALIZADO y se recibieron
     * las 6 bolillas. En cualquier otro caso devuelve null (se sigue esperando).
     */
    fun resultadoFinal(
        estado: EstadoSorteo,
        jugada: List<Int>,
        bolillas: List<Bolilla>
    ): ResultadoFinal? {
        if (estado != EstadoSorteo.FINALIZADO) return null
        if (bolillasPublicadas(bolillas) < TOTAL_BOLILLAS) return null
        return if (aciertos(jugada, bolillas) == TOTAL_BOLILLAS) {
            ResultadoFinal.GANADOR
        } else {
            ResultadoFinal.SIGUE_INTENTANDO
        }
    }

    /** Mensaje informativo según el estado del sorteo. */
    fun mensajeEstado(estado: EstadoSorteo, bolillas: List<Bolilla>): String = when (estado) {
        EstadoSorteo.ABIERTO -> "Sorteo abierto. Esperando el inicio del sorteo…"
        EstadoSorteo.EN_CURSO -> "Sorteo en curso"
        EstadoSorteo.FINALIZADO ->
            if (bolillasPublicadas(bolillas) < TOTAL_BOLILLAS) {
                "Sorteo finalizado. Esperando resultados…"
            } else {
                "Sorteo finalizado"
            }
        EstadoSorteo.DESCONOCIDO -> "Esperando información del sorteo…"
    }
}
