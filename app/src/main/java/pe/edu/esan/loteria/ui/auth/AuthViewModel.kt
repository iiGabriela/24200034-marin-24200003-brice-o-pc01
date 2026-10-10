package pe.edu.esan.loteria.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import pe.edu.esan.loteria.data.repository.AuthRepository
import pe.edu.esan.loteria.data.repository.mensajeErrorAuth

data class AuthUiState(
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val autenticado: Boolean = false
)

/** ViewModel compartido por Login y Registro (cada pantalla tiene su propia instancia). */
class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun onCorreoChange(valor: String) {
        uiState = uiState.copy(correo = valor, error = null)
    }

    fun onContrasenaChange(valor: String) {
        uiState = uiState.copy(contrasena = valor, error = null)
    }

    fun onConfirmacionChange(valor: String) {
        uiState = uiState.copy(confirmacion = valor, error = null)
    }

    fun iniciarSesion() {
        val error = validarComun()
        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }
        ejecutar { authRepository.iniciarSesion(uiState.correo.trim(), uiState.contrasena) }
    }

    fun registrar() {
        val error = validarComun() ?: when {
            uiState.contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            uiState.contrasena != uiState.confirmacion -> "Las contraseñas no coinciden."
            else -> null
        }
        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }
        ejecutar { authRepository.registrar(uiState.correo.trim(), uiState.contrasena) }
    }

    private fun validarComun(): String? = when {
        uiState.correo.isBlank() -> "Ingresa tu correo."
        !Patterns.EMAIL_ADDRESS.matcher(uiState.correo.trim()).matches() ->
            "El formato del correo no es válido."
        uiState.contrasena.isBlank() -> "Ingresa tu contraseña."
        else -> null
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        if (uiState.cargando) return
        uiState = uiState.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                accion()
                uiState = uiState.copy(cargando = false, autenticado = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(cargando = false, error = mensajeErrorAuth(e))
            }
        }
    }
}
