package com.spectrum.phoenix.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixGreen
import com.spectrum.phoenix.ui.theme.PhoenixRed
import kotlinx.coroutines.delay

enum class ToastType { SUCCESS, ERROR, INFO }

class ToastController {
    var message by mutableStateOf("")
    var type by mutableStateOf(ToastType.INFO)
    var isVisible by mutableStateOf(false)

    fun show(msg: String, toastType: ToastType = ToastType.INFO) {
        message = msg
        type = toastType
        isVisible = true
    }
}

val LocalToastController = staticCompositionLocalOf { ToastController() }

@Composable
fun PhoenixToastPopup() {
    val controller = LocalToastController.current
    
    LaunchedEffect(controller.isVisible) {
        if (controller.isVisible) {
            delay(3500) // Un poco más de tiempo para leer
            controller.isVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = controller.isVisible,
            enter = slideInVertically(initialOffsetY = { -it * 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it * 2 }) + fadeOut()
        ) {
            val color = when (controller.type) {
                ToastType.SUCCESS -> PhoenixGreen
                ToastType.ERROR -> PhoenixRed
                ToastType.INFO -> FocusBlue
            }
            
            val icon = when (controller.type) {
                ToastType.SUCCESS -> Icons.Default.CheckCircle
                ToastType.ERROR -> Icons.Default.Error
                ToastType.INFO -> Icons.Default.Info
            }

            // DISEÑO TIPO CÁPSULA DE NOTIFICACIÓN
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 12.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(50.dp), // Totalmente redondeado como cápsula
                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = color, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = controller.message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}
