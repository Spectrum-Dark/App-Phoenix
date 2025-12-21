package com.spectrum.phoenix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.PhoenixToastPopup
import com.spectrum.phoenix.ui.components.ToastController
import com.spectrum.phoenix.ui.login.LoginScreen
import com.spectrum.phoenix.ui.main.MainScreen
import com.spectrum.phoenix.ui.main.productos.almacen.BarcodeViewerScreen
import com.spectrum.phoenix.ui.register.RegisterScreen
import com.spectrum.phoenix.ui.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val toastController = remember { ToastController() }

    CompositionLocalProvider(LocalToastController provides toastController) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController = navController) }
            composable("login") { LoginScreen(navController = navController) }
            composable("register") { RegisterScreen(navController = navController) }
            composable("main") { MainScreen(navController = navController) }
            
            // Vista de código de barras a pantalla completa
            composable(
                route = "barcode/{productId}/{productName}",
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType },
                    navArgument("productName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val productName = backStackEntry.arguments?.getString("productName") ?: ""
                BarcodeViewerScreen(navController, productId, productName)
            }
        }
        
        // El popup siempre estará encima de la navegación
        PhoenixToastPopup()
    }
}
