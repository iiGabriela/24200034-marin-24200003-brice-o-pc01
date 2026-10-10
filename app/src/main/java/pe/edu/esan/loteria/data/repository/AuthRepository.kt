package pe.edu.esan.loteria.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Autenticación con correo y contraseña (Firebase Authentication).
 * Firebase guarda la sesión en el dispositivo: al reabrir la app, currentUser sigue disponible.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    suspend fun registrar(correo: String, contrasena: String): FirebaseUser {
        val resultado = auth.createUserWithEmailAndPassword(correo, contrasena).await()
        return resultado.user ?: error("No se pudo crear la cuenta.")
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): FirebaseUser {
        val resultado = auth.signInWithEmailAndPassword(correo, contrasena).await()
        return resultado.user ?: error("No se pudo iniciar sesión.")
    }

    fun cerrarSesion() = auth.signOut()
}

/** Traduce las excepciones de Firebase Auth a mensajes claros en español. */
fun mensajeErrorAuth(e: Throwable): String = when {
    e is FirebaseAuthException && e.errorCode == "ERROR_INVALID_EMAIL" ->
        "El formato del correo no es válido."
    // WeakPassword hereda de InvalidCredentials, por eso va antes.
    e is FirebaseAuthWeakPasswordException ->
        "La contraseña es muy débil: usa al menos 6 caracteres."
    e is FirebaseAuthUserCollisionException ->
        "Ya existe una cuenta registrada con ese correo."
    e is FirebaseAuthInvalidUserException ->
        "No existe una cuenta con ese correo o fue deshabilitada."
    e is FirebaseAuthInvalidCredentialsException ->
        "Correo o contraseña incorrectos."
    e is FirebaseNetworkException ->
        "Sin conexión a internet. Inténtalo nuevamente."
    e is FirebaseTooManyRequestsException ->
        "Demasiados intentos. Espera un momento e inténtalo otra vez."
    else -> e.localizedMessage ?: "Ocurrió un error inesperado."
}
