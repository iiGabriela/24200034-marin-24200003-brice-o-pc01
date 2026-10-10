package pe.edu.esan.loteria.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.edu.esan.loteria.data.model.EstadoSorteo
import pe.edu.esan.loteria.data.model.Jugada

private const val TAG = "LoteriaListener"

/** Error de negocio al registrar una jugada (sorteo cerrado, inexistente, etc.). */
class JugadaRechazadaException(mensaje: String) : Exception(mensaje)

/**
 * Jugadas guardadas en sorteos/{sorteoId}/jugadas/{uid}.
 * Usar el UID como id del documento garantiza UNA jugada por usuario y por sorteo.
 */
class JugadaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun sorteoRef(sorteoId: String): DocumentReference =
        db.collection(FirestoreRutas.SORTEOS).document(sorteoId)

    private fun jugadaRef(sorteoId: String, uid: String): DocumentReference =
        sorteoRef(sorteoId).collection(FirestoreRutas.JUGADAS).document(uid)

    /** Lectura única de la jugada (para precargar el formulario). */
    suspend fun obtenerJugada(sorteoId: String, uid: String): Jugada? =
        jugadaRef(sorteoId, uid).get().await().aJugada()

    /** Escucha la jugada del usuario de la sesión (solo su documento). */
    fun escucharJugada(sorteoId: String, uid: String): Flow<Jugada?> = callbackFlow {
        Log.d(TAG, "Listener de la jugada de $uid REGISTRADO")
        val registro = jugadaRef(sorteoId, uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.aJugada())
        }
        awaitClose {
            registro.remove()
            Log.d(TAG, "Listener de la jugada de $uid REMOVIDO")
        }
    }

    /**
     * Registra (o modifica) la jugada SOLO si el sorteo está abierto.
     * Se usa una transacción: se lee el estado del sorteo y se escribe la jugada de forma
     * atómica, así no se puede guardar si alguien cambia el estado a "en_curso" al mismo tiempo.
     * Las reglas de Firestore (firestore.rules) repiten estas validaciones en el servidor.
     */
    suspend fun guardarJugada(sorteoId: String, uid: String, correo: String?, numeros: List<Int>) {
        val refSorteo = sorteoRef(sorteoId)
        val refJugada = jugadaRef(sorteoId, uid)

        db.runTransaction { transaccion ->
            val sorteo = transaccion.get(refSorteo)
            if (!sorteo.exists()) {
                throw JugadaRechazadaException("El sorteo no existe.")
            }
            val estado = EstadoSorteo.desde(sorteo.get("estado")?.toString())
            if (estado != EstadoSorteo.ABIERTO) {
                throw JugadaRechazadaException(
                    "El sorteo ya no está abierto: no se pueden registrar ni modificar jugadas."
                )
            }

            val existente = transaccion.get(refJugada)
            val datos = hashMapOf<String, Any>(
                "sorteoId" to sorteoId,
                "uid" to uid,
                "numeros" to numeros.sorted(), // se guardan como valores numéricos
                "actualizadoEn" to FieldValue.serverTimestamp()
            )
            if (correo != null) datos["email"] = correo
            if (!existente.exists()) datos["registradoEn"] = FieldValue.serverTimestamp()

            transaccion.set(refJugada, datos, SetOptions.merge())
            null
        }.await()
    }
}
