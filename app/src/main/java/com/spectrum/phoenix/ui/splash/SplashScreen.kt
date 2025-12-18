package com.spectrum.phoenix.ui.splash

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.ui.theme.FocusBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(key1 = true) {
        delay(2000) // 2 segundos de splash
        
        if (sessionManager.isLoggedIn()) {
            val userName = sessionManager.getUserName() ?: "Usuario"
            navController.navigate("main/$userName") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = FocusBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PHOENIX",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = FocusBlue,
                strokeWidth = 2.dp
            )
        }
    }
}
