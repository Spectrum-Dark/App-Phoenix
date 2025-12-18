package com.spectrum.phoenix.ui.main.reportes

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.reports.ReportesViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

data class ReportOption(
    val title: String,
    val type: String,
    val icon: ImageVector,
    val color: Color,
    val subtitle: String
)

@Composable
fun ReportesScreen(reportesViewModel: ReportesViewModel = viewModel()) {
    val context = LocalContext.current
    
    val inventoryReports = listOf(
        ReportOption("Entradas de Hoy", "ENTRADAS", Icons.Default.MoveToInbox, Color(0xFF4CAF50), "Stock ingresado en la fecha actual"),
        ReportOption("Próximos a Vencer", "PROXIMOS_VENCER", Icons.Default.Timer, Color(0xFFFF9800), "Productos por expirar (7 días)"),
        ReportOption("Productos Vencidos", "VENCIDOS", Icons.Default.EventBusy, Color(0xFFF44336), "Listado de stock ya caducado"),
        ReportOption("Inventario Total", "TODOS_PRODUCTOS", Icons.Default.Inventory, FocusBlue, "Estado actual de todo el almacén")
    )

    val clientReports = listOf(
        ReportOption("Afiliados con Crédito", "CLIENTES_CREDITOS", Icons.Default.CreditCard, Color(0xFFE91E63), "Solo deudores activos"),
        ReportOption("Todos los Clientes", "TODOS_CLIENTES", Icons.Default.People, FocusBlue, "Registro general de afiliados")
    )

    val financeReports = listOf(
        ReportOption("Ventas Generales", "VENTAS_GENERALES", Icons.Default.PointOfSale, Color(0xFF4CAF50), "Ventas de contado (sin crédito)"),
        ReportOption("Ventas con Crédito", "VENTAS_CREDITOS", Icons.Default.ReceiptLong, Color(0xFFFF9800), "Ventas asignadas a afiliados"),
        ReportOption("Reporte General de Ventas", "VENTAS_TOTALES", Icons.Default.Assessment, FocusBlue, "Resumen total de toda la facturación")
    )

    PhoenixTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SECCIÓN: INVENTARIO
            item { ReportSectionTitle("GESTIÓN DE INVENTARIO") }
            items(inventoryReports) { option ->
                ReportListCard(option) { reportesViewModel.generateReport(context, option.type) }
            }

            // SECCIÓN: CLIENTES
            item { ReportSectionTitle("CLIENTES Y CRÉDITOS") }
            items(clientReports) { option ->
                ReportListCard(option) { reportesViewModel.generateReport(context, option.type) }
            }

            // SECCIÓN: VENTAS
            item { ReportSectionTitle("FINANZAS Y VENTAS") }
            items(financeReports) { option ->
                ReportListCard(option) { reportesViewModel.generateReport(context, option.type) }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ReportSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun ReportListCard(option: ReportOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(option.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, null, tint = option.color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(option.title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(option.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.PictureAsPdf, null, tint = option.color.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}
