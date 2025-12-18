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
    val context = LocalContext.current
    
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(deleteResult) {
        deleteResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Operación completada", Toast.LENGTH_SHORT).show()
                vencimientoViewModel.clearResult()
            } else {
                Toast.makeText(context, "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    PhoenixTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vencimientoViewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FocusBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { vencimientoViewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                if (expiringProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isEmpty()) "Sin vencimientos próximos" else "Sin resultados",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Control de Vencimientos",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showDeleteAllConfirm = true }) {
                                    Text("Limpiar todo", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                        
                        items(expiringProducts, key = { it.id }) { product ->
                            ExpiringProductCard(
                                product = product,
                                onDelete = { productToDelete = it }
                            )
                        }
                    }
                }
            }
        }

        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Deseas eliminar '${productToDelete?.name}' definitivamente?") },
                confirmButton = {
                    Button(
                        onClick = { 
                            vencimientoViewModel.deleteProduct(productToDelete!!.id)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) { Text("Cancelar") }
                }
            )
        }

        if (showDeleteAllConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteAllConfirm = false },
                title = { Text("Eliminar Todo") },
                text = { Text("¿Borrar todos los productos listados aquí? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            vencimientoViewModel.deleteAllVisible()
                            showDeleteAllConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Sí, eliminar todo", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun ExpiringProductCard(product: Product, onDelete: (Product) -> Unit) {
    val isExpired = remember(product.expiryDate) { checkIsExpired(product.expiryDate) }
    
    // Colores dinámicos: Rojo para vencido, Amarillo para próximo
    val accentColor = if (isExpired) Color(0xFFE53935) else Color(0xFFFFB300)
    val bgColor = accentColor.copy(alpha = if (isExpired) 0.08f else 0.05f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpired) Icons.Default.ReportProblem else Icons.Default.History,
                    contentDescription = null, 
                    tint = accentColor, 
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = if (isExpired) "VENCIDO: " else "VENCE EN BREVE: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = product.expiryDate ?: "--/--/----",
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(onClick = { onDelete(product) }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun checkIsExpired(dateStr: String?): Boolean {
    if (dateStr == null) return false
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = sdf.parse(dateStr) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        expiryDate.before(today) || expiryDate == today
    } catch (e: Exception) {
        false
    }
}
