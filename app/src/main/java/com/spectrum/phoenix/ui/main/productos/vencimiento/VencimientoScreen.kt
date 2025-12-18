package com.spectrum.phoenix.ui.main.productos.vencimiento

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.vencimiento.VencimientoViewModel
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VencimientoScreen(vencimientoViewModel: VencimientoViewModel = viewModel()) {
    val expiringProducts by vencimientoViewModel.expiringProducts.collectAsStateWithLifecycle()
    val searchQuery by vencimientoViewModel.searchQuery.collectAsStateWithLifecycle()
    val deleteResult by vencimientoViewModel.deleteResult.collectAsStateWithLifecycle()
    val toast = LocalToastController.current // ACTIVADO TOAST PRO
    
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(deleteResult) {
        deleteResult?.let {
            if (it.isSuccess) {
                toast.show("Operación completada", ToastType.SUCCESS)
                vencimientoViewModel.clearResult()
            } else {
                toast.show(it.exceptionOrNull()?.message ?: "Error", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vencimientoViewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Filtrar por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = FocusBlue) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue)
                )

                if (expiringProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No se detectan vencimientos críticos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("ALERTAS DE CADUCIDAD", style = MaterialTheme.typography.labelLarge, color = FocusBlue, fontWeight = FontWeight.Black)
                                IconButton(onClick = { showDeleteAllConfirm = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.DeleteSweep, null, tint = Color.Red)
                                }
                            }
                        }
                        
                        items(expiringProducts, key = { it.id }) { product ->
                            ExpiringProductProCard(product, onDelete = { productToDelete = it })
                        }
                    }
                }
            }
        }

        if (showDeleteAllConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteAllConfirm = false },
                title = { Text("¿Vaciar Alertas?") },
                text = { Text("Se eliminarán todos los productos listados. Esta acción es definitiva y limpiará el stock. ¿Confirmar?") },
                confirmButton = {
                    Button(onClick = { vencimientoViewModel.deleteAllVisible(); showDeleteAllConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("Limpiar Todo", color = Color.White)
                    }
                },
                dismissButton = { TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancelar") } }
            )
        }

        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Deseas eliminar '${productToDelete?.name}' definitivamente?") },
                confirmButton = {
                    Button(onClick = { vencimientoViewModel.deleteProduct(productToDelete!!.id); productToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("Eliminar", color = Color.White)
                    }
                },
                dismissButton = { TextButton(onClick = { productToDelete = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun ExpiringProductProCard(product: Product, onDelete: (Product) -> Unit) {
    val daysLeft = remember(product.expiryDate) { calculateDaysLeft(product.expiryDate) }
    
    val (statusText, statusColor) = when {
        daysLeft <= 0 -> "VENCIDO" to Color(0xFFE53935)
        daysLeft <= 3 -> "ALERTA (3D)" to Color(0xFFFB8C00)
        else -> "AVISO (7D)" to Color(0xFFFBC02D)
    }

    val bgColor = statusColor.copy(alpha = 0.08f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).background(statusColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (daysLeft <= 3) Icons.Default.ReportProblem else Icons.Default.History, 
                    null, 
                    tint = statusColor, 
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(product.expiryDate ?: "--/--/----", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(if(daysLeft <= 0) "Expiró" else "Faltan $daysLeft días", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onDelete(product) }) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun calculateDaysLeft(dateStr: String?): Int {
    if (dateStr == null) return 999
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = sdf.parse(dateStr) ?: return 999
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val diff = expiryDate.time - today.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) { 999 }
}
