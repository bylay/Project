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
import com.example.project.screens.HistorialAccesoScreen




sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")

    object Dashboard : AppScreen("dashboard_screen/{email}") {
        fun crearRuta(email: String) = "dashboard_screen/$email"
    }

    object Horarios : AppScreen("horarios_screen/{email}") {
        fun crearRuta(email: String) = "horarios_screen/$email"
    }

    object BloquearBarrera : AppScreen("bloquear_screen/{email}") {
        fun crearRuta(email: String) = "bloquear_screen/$email"
    }

    object HistorialAcceso : AppScreen("historial_screen")
}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreen.Login.route) {

        composable(AppScreen.Login.route) {
            LoginScreen(navController)
        }

        composable(
            route = AppScreen.Dashboard.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            DashboardScreen(navController, email)
        }

        composable(
            route = AppScreen.Horarios.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            HorariosScreen(navController, email)
        }

        composable(
            route = AppScreen.BloquearBarrera.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            BloquearScreen(navController, email)
        }
        composable(AppScreen.HistorialAcceso.route) {
            HistorialAccesoScreen(navController)
        }


    }
}
