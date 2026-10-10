package pe.edu.esan.loteria.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.ui.components.BolillaCirculo

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val estado = viewModel.uiState
    LaunchedEffect(estado.autenticado) {
        if (estado.autenticado) onLoginExitoso()
    }
    FormularioAuth(
        titulo = "Iniciar sesión",
        estado = estado,
        mostrarConfirmacion = false,
        textoBoton = "Ingresar",
        onCorreoChange = viewModel::onCorreoChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onConfirmacionChange = {},
        onEnviar = viewModel::iniciarSesion,
        textoEnlace = "¿No tienes cuenta? Regístrate",
        onEnlace = onIrARegistro
    )
}

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val estado = viewModel.uiState
    LaunchedEffect(estado.autenticado) {
        if (estado.autenticado) onRegistroExitoso()
    }
    FormularioAuth(
        titulo = "Crear cuenta",
        estado = estado,
        mostrarConfirmacion = true,
        textoBoton = "Registrarme",
        onCorreoChange = viewModel::onCorreoChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onConfirmacionChange = viewModel::onConfirmacionChange,
        onEnviar = viewModel::registrar,
        textoEnlace = "¿Ya tienes cuenta? Inicia sesión",
        onEnlace = onIrALogin
    )
}

@Composable
private fun FormularioAuth(
    titulo: String,
    estado: AuthUiState,
    mostrarConfirmacion: Boolean,
    textoBoton: String,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onEnviar: () -> Unit,
    textoEnlace: String,
    onEnlace: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BolillaCirculo(numero = 45, resaltada = false, tamano = 72.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Lotería ESAN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = estado.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                enabled = !estado.cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = onContrasenaChange,
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !estado.cargando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mostrarConfirmacion) ImeAction.Next else ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarConfirmacion) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = estado.confirmacion,
                    onValueChange = onConfirmacionChange,
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (estado.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = estado.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onEnviar,
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(textoBoton)
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onEnlace, enabled = !estado.cargando) {
                Text(textoEnlace)
            }
        }
    }
}
