package pe.edu.esan.loteria.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.edu.esan.loteria.data.repository.AuthRepository
import pe.edu.esan.loteria.ui.auth.LoginScreen
import pe.edu.esan.loteria.ui.auth.RegistroScreen
import pe.edu.esan.loteria.ui.jugada.JugadaScreen
import pe.edu.esan.loteria.ui.seguimiento.SeguimientoScreen
import pe.edu.esan.loteria.ui.sorteos.SorteosScreen

/**
 * Flujo: Registro <-> Login -> Sorteos -> Jugada -> Seguimiento.
 * Todas las pantallas tienen opción para volver o avanzar, y cerrar sesión regresa al Login.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    // Si Firebase conserva la sesión, se entra directo a la lista de sorteos.
    val inicio = remember { if (authRepository.usuarioActual != null) Rutas.SORTEOS else Rutas.LOGIN }

    val correo: () -> String? = { authRepository.usuarioActual?.email }
    val cerrarSesion: () -> Unit = {
        authRepository.cerrarSesion()
        navController.irALimpiando(Rutas.LOGIN)
    }

    NavHost(navController = navController, startDestination = inicio) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { navController.irALimpiando(Rutas.SORTEOS) },
                onIrARegistro = {
                    navController.navigate(Rutas.REGISTRO) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = { navController.irALimpiando(Rutas.SORTEOS) },
                onIrALogin = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.REGISTRO) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Rutas.SORTEOS) {
            SorteosScreen(
                correo = correo(),
                onSeleccionarSorteo = { id -> navController.navigate(Rutas.jugada(id)) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Rutas.JUGADA,
            arguments = listOf(navArgument(Rutas.ARG_SORTEO_ID) { type = NavType.StringType })
        ) {
            JugadaScreen(
                correo = correo(),
                onVolver = { navController.volverOIr(Rutas.SORTEOS) },
                onVerSeguimiento = { id -> navController.navigate(Rutas.seguimiento(id)) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Rutas.SEGUIMIENTO,
            arguments = listOf(navArgument(Rutas.ARG_SORTEO_ID) { type = NavType.StringType })
        ) {
            SeguimientoScreen(
                correo = correo(),
                onVolver = { navController.volverOIr(Rutas.SORTEOS) },
                onIrASorteos = {
                    if (!navController.popBackStack(Rutas.SORTEOS, inclusive = false)) {
                        navController.irALimpiando(Rutas.SORTEOS)
                    }
                },
                onCerrarSesion = cerrarSesion
            )
        }
    }
}

/** Navega a [ruta] borrando toda la pila (tras login/registro o al cerrar sesión). */
private fun NavHostController.irALimpiando(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Vuelve a la pantalla anterior; si no hay ninguna, abre [respaldo]. */
private fun NavHostController.volverOIr(respaldo: String) {
    if (previousBackStackEntry != null) popBackStack() else irALimpiando(respaldo)
}
