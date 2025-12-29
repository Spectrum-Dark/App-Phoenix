package com.spectrum.phoenix.ui.main.configuracion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.configuracion.BackupViewModel
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun BackupScreen(backupViewModel: BackupViewModel = viewModel()) {
    val context = LocalContext.current
    val toast = LocalToastController.current
    val result by backupViewModel.opResult.collectAsStateWithLifecycle()
    val isLoading by backupViewModel.isLoading.collectAsStateWithLifecycle()
    
    var showImportConfirm by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { showImportConfirm = it }
    }

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                toast.show(it.getOrNull() ?: "Operación exitosa", ToastType.SUCCESS)
                backupViewModel.clearResult()
            } else {
                toast.show("Error: ${it.exceptionOrNull()?.message}", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CloudSync, null, tint = FocusBlue, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Centro de Seguridad", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Gestiona el respaldo local de tus datos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(40.dp))

            BackupActionCard(
                title = "Exportar Base de Datos",
                description = "Genera un archivo JSON con toda la información actual y guárdalo en tu carpeta de Descargas.",
                icon = Icons.Default.CloudDownload,
                color = FocusBlue,
                isLoading = isLoading,
                onClick = { backupViewModel.exportDatabase(context) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            BackupActionCard(
                title = "Importar/Restaurar Datos",
                description = "Selecciona un archivo de respaldo previo para sobrescribir la base de datos actual. ¡Atención: Esto borrará los datos actuales!",
                icon = Icons.Default.CloudUpload,
                color = Color(0xFFE91E63),
                isLoading = isLoading,
                onClick = { filePicker.launch("application/json") }
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = FocusBlue)
            }
        }

        if (showImportConfirm != null) {
            AlertDialog(
                onDismissRequest = { showImportConfirm = null },
                title = { Text("¿Restaurar Base de Datos?") },
                text = { Text("Estás a punto de reemplazar toda la información actual con los datos del archivo seleccionado. Esta acción es definitiva.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            backupViewModel.importDatabase(context, showImportConfirm!!)
                            showImportConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("SÍ, RESTAURAR AHORA", color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showImportConfirm = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun BackupActionCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isLoading: Boolean, onClick: () -> Unit) {
    Card(
        onClick = if(!isLoading) onClick else ({}),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}
