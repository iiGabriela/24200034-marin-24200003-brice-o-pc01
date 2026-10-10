package pe.edu.esan.loteria.data.model

/**
 * Estados posibles del sorteo. En Firestore se guardan como texto en minúsculas:
 * "abierto", "en_curso" o "finalizado" (también se acepta "en curso").
 */
enum class EstadoSorteo(val valorFirestore: String, val etiqueta: String) {
    ABIERTO("abierto", "Abierto"),
    EN_CURSO("en_curso", "En curso"),
    FINALIZADO("finalizado", "Finalizado"),
    DESCONOCIDO("", "Sin estado");

    companion object {
        fun desde(valor: String?): EstadoSorteo {
            val normalizado = valor
                ?.trim()
                ?.lowercase()
                ?.replace(' ', '_')
                ?.replace('-', '_')
                ?: return DESCONOCIDO
            return entries.firstOrNull { it != DESCONOCIDO && it.valorFirestore == normalizado }
                ?: DESCONOCIDO
        }
    }
}

/** Cabecera del sorteo: sorteos/{sorteoId} */
data class Sorteo(
    val id: String,
    val nombre: String,
    val fecha: String,
    val estado: EstadoSorteo
)

/** Bolilla publicada: sorteos/{sorteoId}/bolillas/{bolillaId} */
data class Bolilla(
    val id: String,
    val sorteoId: String,
    val numero: Int,
    val orden: Int
)

/** Jugada del usuario: sorteos/{sorteoId}/jugadas/{uid} */
data class Jugada(
    val sorteoId: String,
    val uid: String,
    val numeros: List<Int>
)
