package com.spectrum.phoenix.ui.main.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun BackupScreen() {
    PhoenixTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(FocusBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CloudSync, null, tint = FocusBlue, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Centro de Respaldos", fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Tus datos están protegidos automáticamente en la nube", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = FocusBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Estado de la Sincronización", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Toda tu información de productos, clientes y ventas se sincroniza en tiempo real con Firebase.", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Lógica futura */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FocusBlue)
            ) {
                Icon(Icons.Default.CloudDownload, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("EXPORTAR DATOS LOCALES", fontWeight = FontWeight.Bold)
            }
        }
    }
}
