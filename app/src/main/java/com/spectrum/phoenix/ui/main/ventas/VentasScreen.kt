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
import androidx.compose.ui.graphics.Brush
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
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(ventasViewModel: VentasViewModel = viewModel()) {
    val context = LocalContext.current
    val toast = LocalToastController.current // ACTIVADO TOAST PRO
    
    val cartItems by ventasViewModel.cartItems.collectAsStateWithLifecycle()
    val total by ventasViewModel.total.collectAsStateWithLifecycle()
    val selectedClient by ventasViewModel.selectedClient.collectAsStateWithLifecycle()
    val filteredProducts by ventasViewModel.filteredProducts.collectAsStateWithLifecycle()
    val filteredClients by ventasViewModel.filteredClients.collectAsStateWithLifecycle()
    val searchQuery by ventasViewModel.productSearchQuery.collectAsStateWithLifecycle()
    val clientSearchQuery by ventasViewModel.clientSearchQuery.collectAsStateWithLifecycle()
    val result by ventasViewModel.saleResult.collectAsStateWithLifecycle()

    var showQtyDialog by remember { mutableStateOf<Product?>(null) }
    var showClientPicker by remember { mutableStateOf(false) }
    
    val phoenixGreen = MaterialTheme.colorScheme.secondary
    val phoenixBlue = MaterialTheme.colorScheme.primary

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                toast.show("Operación exitosa", ToastType.SUCCESS)
                ventasViewModel.clearResult()
            } else {
                toast.show("Error: ${it.exceptionOrNull()?.message}", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                Card(
                    onClick = { showClientPicker = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = phoenixBlue.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, phoenixBlue.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(phoenixBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = phoenixBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PUNTO DE VENTA", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = phoenixBlue, letterSpacing = 1.sp)
                            Text(selectedClient?.let { "${it.name} ${it.lastName}" } ?: "Venta General", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.Default.UnfoldMore, null, tint = phoenixBlue.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { ventasViewModel.onProductSearchQueryChange(it) },
                        placeholder = { Text("Buscar producto...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = phoenixBlue, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = phoenixBlue)
                    )

                    if (filteredProducts.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 54.dp).heightIn(max = 240.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            LazyColumn {
                                itemsIndexed(filteredProducts) { _, product ->
                                    ListItem(
                                        headlineContent = { Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                        supportingContent = { 
                                            Text(buildAnnotatedString {
                                                append("Stock: ${product.quantity}  •  ")
                                                withStyle(style = SpanStyle(color = phoenixGreen, fontWeight = FontWeight.Bold)) {
                                                    append("C$ ${String.format("%.2f", product.price)}")
                                                }
                                            }, fontSize = 11.sp)
                                        },
                                        modifier = Modifier.clickable { showQtyDialog = product },
                                        trailingContent = { Icon(Icons.Default.AddCircle, null, tint = phoenixBlue, modifier = Modifier.size(22.dp)) }
                                    )
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ListAlt, null, tint = phoenixBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REGISTRO DE ITEMS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = phoenixBlue, letterSpacing = 0.5.sp)
                }
                
                Box(modifier = Modifier.weight(1f).padding(bottom = 100.dp)) {
                    if (cartItems.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                            Text("No hay registros", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                            itemsIndexed(cartItems) { _, item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${item.quantity} un. x C$ ${item.price}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("C$ ${String.format("%.2f", item.subtotal)}", fontWeight = FontWeight.Black, color = phoenixGreen, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { ventasViewModel.removeFromCart(item) }, modifier = Modifier.size(28.dp)) {
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
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL A COBRAR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text("C$ ${String.format("%.2f", total)}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = phoenixGreen)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { ventasViewModel.finalizeSale() },
                        enabled = cartItems.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = phoenixBlue, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PointOfSale, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COBRAR OPERACIÓN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (showClientPicker) {
            AlertDialog(onDismissRequest = { showClientPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Surface(modifier = Modifier.fillMaxWidth(0.88f).wrapContentHeight().imePadding(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Seleccionar Cliente", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = clientSearchQuery, onValueChange = { ventasViewModel.onClientSearchQueryChange(it) }, placeholder = { Text("Buscar...", fontSize = 13.sp) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.heightIn(max = 280.dp)) {
                            LazyColumn {
                                item {
                                    ListItem(headlineContent = { Text("Venta General", fontSize = 13.sp, fontWeight = FontWeight.Bold) }, leadingContent = { Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, null, modifier = Modifier.size(20.dp)) } }, modifier = Modifier.clickable { ventasViewModel.selectClient(null); showClientPicker = false })
                                }
                                itemsIndexed(filteredClients) { _, client ->
                                    ListItem(headlineContent = { Text("${client.name} ${client.lastName}", fontSize = 13.sp) }, leadingContent = { Box(modifier = Modifier.size(32.dp).background(phoenixBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Text(client.name.take(1).uppercase(), color = phoenixBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp) } }, modifier = Modifier.clickable { ventasViewModel.selectClient(client); showClientPicker = false })
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showQtyDialog != null) {
            val product = showQtyDialog!!
            var qty by remember { mutableIntStateOf(1) }
            AlertDialog(onDismissRequest = { showQtyDialog = null }) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.85f)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(product.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (qty > 1) qty-- }, modifier = Modifier.size(48.dp).background(phoenixBlue.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Default.Remove, null, tint = phoenixBlue) }
                            Text(text = "$qty", fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 24.dp))
                            IconButton(onClick = { if (qty < product.quantity) qty++ }, modifier = Modifier.size(48.dp).background(phoenixBlue.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Default.Add, null, tint = phoenixBlue) }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("C$ ${String.format("%.2f", qty * product.price)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = phoenixGreen)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { ventasViewModel.addToCart(product, qty); showQtyDialog = null }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = phoenixBlue)) {
                            Text("AÑADIR", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
