package com.spectrum.phoenix.ui.main.ventas

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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.model.Client
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.model.SaleItem
import com.spectrum.phoenix.logic.ventas.VentasViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(ventasViewModel: VentasViewModel = viewModel()) {
    val context = LocalContext.current
    val cartItems by ventasViewModel.cartItems.collectAsStateWithLifecycle()
    val total by ventasViewModel.total.collectAsStateWithLifecycle()
    val selectedClient by ventasViewModel.selectedClient.collectAsStateWithLifecycle()
    val filteredProducts by ventasViewModel.filteredProducts.collectAsStateWithLifecycle()
    val allClients by ventasViewModel.allClients.collectAsStateWithLifecycle()
    val searchQuery by ventasViewModel.productSearchQuery.collectAsStateWithLifecycle()
    val result by ventasViewModel.saleResult.collectAsStateWithLifecycle()

    var showQtyDialog by remember { mutableStateOf<Product?>(null) }
    var showClientPicker by remember { mutableStateOf(false) }
    val priceGreen = Color(0xFF4CAF50)

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Venta realizada", Toast.LENGTH_SHORT).show()
                ventasViewModel.clearResult()
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
                    .padding(horizontal = 14.dp)
            ) {
                Card(
                    onClick = { showClientPicker = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FocusBlue.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(FocusBlue.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = FocusBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AFILIADO / CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = FocusBlue)
                            Text(selectedClient?.let { "${it.name} ${it.lastName}" } ?: "Venta General", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Icon(Icons.Default.UnfoldMore, null, tint = FocusBlue.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { ventasViewModel.onProductSearchQueryChange(it) },
                        placeholder = { Text("Buscar producto...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue)
                    )

                    if (filteredProducts.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 54.dp).heightIn(max = 240.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            LazyColumn {
                                itemsIndexed(filteredProducts) { _, product ->
                                    ListItem(
                                        headlineContent = { Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                        supportingContent = { 
                                            Text(buildAnnotatedString {
                                                append("Stock: ${product.quantity}  •  ")
                                                withStyle(style = SpanStyle(color = priceGreen, fontWeight = FontWeight.Bold)) {
                                                    append("C$ ${String.format("%.2f", product.price)}")
                                                }
                                            }, fontSize = 12.sp)
                                        },
                                        modifier = Modifier.clickable { showQtyDialog = product },
                                        trailingContent = { Icon(Icons.Default.AddCircle, null, tint = FocusBlue, modifier = Modifier.size(24.dp)) }
                                    )
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("DETALLE DE VENTA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = FocusBlue)
                
                Box(modifier = Modifier.weight(1f).padding(bottom = 110.dp)) {
                    if (cartItems.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                            Text("Carrito vacío", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            itemsIndexed(cartItems) { _, item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = FocusBlue.copy(alpha = 0.02f)),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${item.quantity} un. x C$ ${item.price}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("C$ ${String.format("%.2f", item.subtotal)}", fontWeight = FontWeight.Black, color = priceGreen, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { ventasViewModel.removeFromCart(item) }, modifier = Modifier.size(30.dp)) {
                                            Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                tonalElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("C$ ${String.format("%.2f", total)}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = priceGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { ventasViewModel.finalizeSale() },
                        enabled = cartItems.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusBlue, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PointOfSale, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COBRAR AHORA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (showQtyDialog != null) {
            val product = showQtyDialog!!
            var qty by remember { mutableIntStateOf(1) }
            AlertDialog(onDismissRequest = { showQtyDialog = null }) {
                Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(product.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Default.Remove, null, tint = FocusBlue) }
                            Text(text = "$qty", fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp))
                            IconButton(onClick = { if (qty < product.quantity) qty++ }) { Icon(Icons.Default.Add, null, tint = FocusBlue) }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("C$ ${String.format("%.2f", qty * product.price)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = priceGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { ventasViewModel.addToCart(product, qty); showQtyDialog = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FocusBlue)) {
                            Text("AÑADIR", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showClientPicker) {
            AlertDialog(onDismissRequest = { showClientPicker = false }) {
                Surface(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp), shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Seleccionar Cliente", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn {
                            item {
                                ListItem(
                                    headlineContent = { Text("Venta General", fontSize = 14.sp) },
                                    leadingContent = { 
                                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Groups, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    modifier = Modifier.clickable { ventasViewModel.selectClient(null); showClientPicker = false }
                                )
                            }
                            itemsIndexed(allClients) { _, client ->
                                ListItem(
                                    headlineContent = { Text("${client.name} ${client.lastName}", fontSize = 14.sp) },
                                    leadingContent = { 
                                        Box(
                                            modifier = Modifier.size(36.dp).background(FocusBlue.copy(0.1f), CircleShape), 
                                            contentAlignment = Alignment.Center
                                        ) { 
                                            Text(client.name.take(1).uppercase(), color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp) 
                                        } 
                                    },
                                    modifier = Modifier.clickable { ventasViewModel.selectClient(client); showClientPicker = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
