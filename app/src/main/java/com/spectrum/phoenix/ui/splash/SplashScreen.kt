package com.spectrum.phoenix.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.logic.user.UserRepository
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userRepository = remember { UserRepository() }

    LaunchedEffect(Unit) {
        // Verificar y crear admin por defecto si no existe
        userRepository.checkAndCreateAdmin()
        
        delay(2000)
        if (sessionManager.isLoggedIn()) {
            navController.navigate("main") { popUpTo("splash") { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }

    PhoenixTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(FocusBlue.copy(alpha = 0.05f), MaterialTheme.colorScheme.background)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(FocusBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = FocusBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "PHOENIX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = FocusBlue,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "ENTERPRISE SYSTEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                LinearProgressIndicator(
                    modifier = Modifier.width(100.dp).height(2.dp),
                    color = FocusBlue,
                    trackColor = FocusBlue.copy(alpha = 0.1f)
                )
            }
            
            Text(
                text = "Powered by Spectrum",
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
