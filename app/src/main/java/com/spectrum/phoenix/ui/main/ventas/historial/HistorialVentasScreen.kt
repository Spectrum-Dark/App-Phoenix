package com.spectrum.phoenix.ui.main.ventas.historial

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.model.Sale
import com.spectrum.phoenix.logic.ventas.HistorialVentasViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentasScreen(historialViewModel: HistorialVentasViewModel = viewModel()) {
    val context = LocalContext.current
    val sales by historialViewModel.filteredSales.collectAsStateWithLifecycle()
    val searchQuery by historialViewModel.searchQuery.collectAsStateWithLifecycle()
    val result by historialViewModel.opResult.collectAsStateWithLifecycle()

    var selectedSaleForDetails by remember { mutableStateOf<Sale?>(null) }
    var saleToRevert by remember { mutableStateOf<Sale?>(null) }
    val priceGreen = Color(0xFF4CAF50)

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Operación exitosa", Toast.LENGTH_SHORT).show()
                selectedSaleForDetails = null
                saleToRevert = null
                historialViewModel.clearResult()
            } else {
                Toast.makeText(context, "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    PhoenixTheme {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { historialViewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar por cliente o fecha...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = FocusBlue) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue)
            )

            if (sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay ventas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text("REGISTRO DE VENTAS", style = MaterialTheme.typography.labelLarge, color = FocusBlue, fontWeight = FontWeight.Black)
                    }
                    itemsIndexed(sales) { _, sale ->
                        SaleHistoryCard(
                            sale = sale,
                            onDetails = { selectedSaleForDetails = sale }
                        )
                    }
                }
            }
        }

        // Diálogo de Detalles de Venta
        if (selectedSaleForDetails != null) {
            SaleDetailsDialog(
                sale = selectedSaleForDetails!!,
                onDismiss = { selectedSaleForDetails = null },
                onRevertRequest = { saleToRevert = it }
            )
        }

        // Confirmación de Reversión
        if (saleToRevert != null) {
            AlertDialog(
                onDismissRequest = { saleToRevert = null },
                title = { Text("¿Revertir Venta?") },
                text = { Text("Esta acción devolverá los productos al stock y anulará el cargo (si fue crédito). Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = { historialViewModel.revertirVenta(saleToRevert!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, Revertir", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { saleToRevert = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun SaleHistoryCard(sale: Sale, onDetails: () -> Unit) {
    val priceGreen = Color(0xFF4CAF50)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ReceiptLong, null, tint = FocusBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sale.clientName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(sale.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("C$ ${String.format("%.2f", sale.total)}", color = priceGreen, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailsDialog(sale: Sale, onDismiss: () -> Unit, onRevertRequest: (Sale) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Detalle de Venta", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(sale.clientName, color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(sale.date, fontSize = 12.sp, color = Color.Gray)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sale.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.productName}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("C$ ${String.format("%.2f", item.subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", fontWeight = FontWeight.Bold)
                    Text("C$ ${String.format("%.2f", sale.total)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onRevertRequest(sale) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Undo, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REVERTIR VENTA", fontWeight = FontWeight.Bold, color = Color.White)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
