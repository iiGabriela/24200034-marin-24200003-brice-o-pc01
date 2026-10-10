package pe.edu.esan.loteria.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pe.edu.esan.loteria.data.model.Bolilla
import pe.edu.esan.loteria.data.model.Sorteo

private const val TAG = "LoteriaListener"

/**
 * Escucha en tiempo real la cabecera del sorteo y sus bolillas.
 *
 * Cada función envuelve un addSnapshotListener dentro de un callbackFlow:
 *  - Firestore llama al listener cada vez que cambia el documento/colección
 *    (por ejemplo, cuando se publica una bolilla desde la consola).
 *  - trySend() emite el nuevo valor hacia la pantalla.
 *  - awaitClose { registro.remove() } quita el listener cuando nadie sigue observando
 *    (al salir de la pantalla), evitando listeners duplicados o activos de más.
 */
class SorteoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun escucharSorteos(): Flow<List<Sorteo>> = callbackFlow {
        Log.d(TAG, "Listener de lista de sorteos REGISTRADO")
        val registro = db.collection(FirestoreRutas.SORTEOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sorteos = snapshot?.documents
                    ?.mapNotNull { it.aSorteo() }
                    ?.sortedBy { it.nombre }
                    .orEmpty()
                trySend(sorteos)
            }
        awaitClose {
            registro.remove()
            Log.d(TAG, "Listener de lista de sorteos REMOVIDO")
        }
    }

    fun escucharSorteo(sorteoId: String): Flow<Sorteo?> = callbackFlow {
        Log.d(TAG, "Listener del sorteo $sorteoId REGISTRADO")
        val registro = db.collection(FirestoreRutas.SORTEOS).document(sorteoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.aSorteo())
            }
        awaitClose {
            registro.remove()
            Log.d(TAG, "Listener del sorteo $sorteoId REMOVIDO")
        }
    }

    /** Bolillas publicadas, ordenadas por su orden de salida (1 a 6). */
    fun escucharBolillas(sorteoId: String): Flow<List<Bolilla>> = callbackFlow {
        Log.d(TAG, "Listener de bolillas de $sorteoId REGISTRADO")
        val registro = db.collection(FirestoreRutas.SORTEOS).document(sorteoId)
            .collection(FirestoreRutas.BOLILLAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bolillas = snapshot?.documents
                    ?.mapNotNull { it.aBolilla(sorteoId) }
                    ?.sortedBy { it.orden }
                    .orEmpty()
                Log.d(TAG, "Bolillas recibidas: ${bolillas.map { it.numero }}")
                trySend(bolillas)
            }
        awaitClose {
            registro.remove()
            Log.d(TAG, "Listener de bolillas de $sorteoId REMOVIDO")
        }
    }
}
