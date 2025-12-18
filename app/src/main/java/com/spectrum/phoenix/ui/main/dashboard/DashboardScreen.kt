package com.spectrum.phoenix.ui.main.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.dashboard.DashboardViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun DashboardScreen(dashboardViewModel: DashboardViewModel = viewModel()) {
    val ventas by dashboardViewModel.ventasGenerales.collectAsStateWithLifecycle()
    val creditos by dashboardViewModel.totalCreditos.collectAsStateWithLifecycle()
    val pRegistrados by dashboardViewModel.totalProductosRegistrados.collectAsStateWithLifecycle()
    val stockTotal by dashboardViewModel.totalStockFisico.collectAsStateWithLifecycle()
    val clientes by dashboardViewModel.totalClientes.collectAsStateWithLifecycle()
    val logs by dashboardViewModel.activityLogs.collectAsStateWithLifecycle()

    val totalGlobal = ventas + creditos
    val priceGreen = Color(0xFF4CAF50)

    PhoenixTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardCard(Modifier.weight(1f), "VENTAS", "C$ ${String.format("%.2f", ventas)}", Icons.Default.Payments, priceGreen)
                DashboardCard(Modifier.weight(1f), "CRÉDITOS", "C$ ${String.format("%.2f", creditos)}", Icons.Default.CreditCard, Color(0xFFE91E63))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FocusBlue.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusBlue.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TOTAL GLOBAL (VENTAS + CRÉDITOS)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FocusBlue)
                        Text("C$ ${String.format("%.2f", totalGlobal)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = priceGreen)
                    }
                    Box(modifier = Modifier.size(40.dp).background(FocusBlue.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = FocusBlue, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QueryStats, null, tint = FocusBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ESTADÍSTICAS DE CONTROL", fontWeight = FontWeight.Black, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(16.dp))
                StatRow("Productos Registrados", pRegistrados.toString(), Icons.Default.Inventory)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                StatRow("Unidades en Stock Total", stockTotal.toString(), Icons.Default.Warehouse)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                StatRow("Total Afiliados", clientes.toString(), Icons.Default.People)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = FocusBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACTIVIDAD RECIENTE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = FocusBlue)
            }

            Box(modifier = Modifier.weight(1f)) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Esperando actividad...", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
                    ) {
                        items(logs) { log ->
                            ActivityItem(log.action, log.details, log.time) // USAR log.time DIRECTAMENTE
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(30.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = FocusBlue, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(value, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ActivityItem(action: String, details: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.padding(top = 6.dp).size(6.dp).background(FocusBlue, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = time, 
                        fontSize = 10.sp, 
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}
