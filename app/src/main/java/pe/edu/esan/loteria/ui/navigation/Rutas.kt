package pe.edu.esan.loteria.ui.navigation

import android.net.Uri

/** Rutas de navegación. El id del sorteo viaja como argumento en la ruta. */
object Rutas {
    const val ARG_SORTEO_ID = "sorteoId"

    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val SORTEOS = "sorteos"
    const val JUGADA = "jugada/{$ARG_SORTEO_ID}"
    const val SEGUIMIENTO = "seguimiento/{$ARG_SORTEO_ID}"

    fun jugada(sorteoId: String) = "jugada/${Uri.encode(sorteoId)}"
    fun seguimiento(sorteoId: String) = "seguimiento/${Uri.encode(sorteoId)}"
}
