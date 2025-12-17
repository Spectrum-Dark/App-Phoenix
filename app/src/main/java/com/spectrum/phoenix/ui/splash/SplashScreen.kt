package com.spectrum.phoenix.ui.splash

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    Text("Splash Screen")
    LaunchedEffect(key1 = true) {
        delay(2000) // 2 second delay
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }
}