package com.spectrum.phoenix.ui.main.configuracion

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.config.UpdateState
import com.spectrum.phoenix.logic.config.UpdateViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun UpdateScreen(updateViewModel: UpdateViewModel = viewModel()) {
    val state by updateViewModel.updateState.collectAsStateWithLifecycle()
    val progress by updateViewModel.downloadProgress.collectAsStateWithLifecycle()

    PhoenixTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ICONO CENTRAL CON ANIMACIÓN SEGÚN EL ESTADO
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(FocusBlue.copy(alpha = 0.1f), FocusBlue.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (state) {
                    is UpdateState.Available -> Icons.Default.SystemUpdate
                    is UpdateState.UpToDate -> Icons.Default.CloudDone
                    is UpdateState.Error -> Icons.Default.ErrorOutline
                    else -> Icons.Default.CloudSync
                }
                
                val iconColor = when (state) {
                    is UpdateState.UpToDate -> Color(0xFF4CAF50)
                    is UpdateState.Error -> MaterialTheme.colorScheme.error
                    else -> FocusBlue
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = iconColor
                )
                
                if (state is UpdateState.Checking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = FocusBlue,
                        strokeWidth = 2.dp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val title = when (state) {
                    is UpdateState.Checking -> "Buscando..."
                    is UpdateState.Available -> "¡Nueva Versión Disponible!"
                    is UpdateState.UpToDate -> "Sistema Actualizado"
                    is UpdateState.Downloading -> "Descargando..."
                    is UpdateState.ReadyToInstall -> "Descarga Completa"
                    is UpdateState.Error -> "Error de Conexión"
                    else -> "Actualizaciones"
                }
                
                val subtitle = when (val s = state) {
                    is UpdateState.Available -> "Versión ${s.info.latestVersion} lista para descargar"
                    is UpdateState.UpToDate -> "Phoenix Enterprise está en su última versión"
                    is UpdateState.Error -> s.message
                    is UpdateState.Downloading -> "Obteniendo datos desde GitHub"
                    else -> "Verifica si hay mejoras disponibles"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // TARJETA DE INFORMACIÓN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoItem("Versión Instalada", updateViewModel.currentVersion)
                        VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp)
                        InfoItem("Canal", "Oficial GitHub")
                    }
                    
                    if (state is UpdateState.Available) {
                        val info = (state as UpdateState.Available).info
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Notas de la versión:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FocusBlue
                        )
                        Text(
                            text = info.releaseNotes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ACCIONES
            when (val s = state) {
                is UpdateState.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = FocusBlue,
                            trackColor = FocusBlue.copy(alpha = 0.1f)
                        )
                        Text(
                            "Procesando... ${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusBlue
                        )
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = { updateViewModel.installApk(s.file) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.InstallMobile, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("INSTALAR AHORA", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = { 
                            if (state is UpdateState.Available) {
                                updateViewModel.downloadAndInstall((state as UpdateState.Available).info.apkUrl)
                            } else {
                                updateViewModel.checkForUpdates()
                            }
                        },
                        enabled = state !is UpdateState.Checking,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state is UpdateState.Available) Color(0xFF4CAF50) else FocusBlue
                        )
                    ) {
                        Icon(
                            imageVector = if (state is UpdateState.Available) Icons.Default.Download else Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (state is UpdateState.Available) "DESCARGAR E INSTALAR" else "BUSCAR ACTUALIZACIONES",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                "ID de dispositivo: ${System.currentTimeMillis().toString().takeLast(8)}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
