package com.example.project.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project.screens.LoginScreen
import com.example.project.screens.DashboardScreen
import com.example.project.screens.HorariosScreen
import com.example.project.screens.BloquearScreen


sealed class  AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Dashboard : AppScreen("dashboard_screen/{usuario}") {
        fun crearRuta(usuario: String) = "dashboard_screen/$usuario"
    }
    object HistorialAcceso : AppScreen("historial_screen")
    object BloquearBarrera : AppScreen("bloquear_screen")
    object Horarios : AppScreen("horarios_screen")

}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.Login.route
    ) {
        // pantalla login
        composable(AppScreen.Login.route) {
            LoginScreen(navController = navController)
        }
        // pantalla dashboard
        composable(
            route = AppScreen.Dashboard.route,
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: "Invitado"
            DashboardScreen(navController, usuario)
        }
        // pantalla horarios
        composable(AppScreen.Horarios.route) {
            HorariosScreen(navController = navController)
        }
        // pantalla barrera
        composable(AppScreen.BloquearBarrera.route) {
            BloquearScreen(navController = navController)
        }
    }
}

