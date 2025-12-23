package com.spectrum.phoenix.ui.main.productos.almacen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.almacen.ProductViewModel
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Composable
fun AlmacenScreen(navController: NavController, productViewModel: ProductViewModel = viewModel()) {
    val products by productViewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by productViewModel.searchQuery.collectAsStateWithLifecycle()
    val result by productViewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toast = LocalToastController.current
    val sessionManager = remember { SessionManager(context) }
    val userRole = sessionManager.getUserRole()
    val isAdmin = userRole == "admin"
    
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val isDialogOpen = showAddDialog || productToEdit != null

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                toast.show("Operación exitosa", ToastType.SUCCESS)
                showAddDialog = false
                productToEdit = null
                productToDelete = null
                productViewModel.clearResult()
            } else {
                toast.show("Error: ${it.exceptionOrNull()?.message}", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { productViewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar producto...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = FocusBlue) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).focusProperties { canFocus = !isDialogOpen },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue)
                )

                if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos en el almacén", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
                    ) {
                        item {
                            Text("${products.size} productos registrados", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product, 
                                isAdmin = isAdmin,
                                onEdit = { productToEdit = it }, 
                                onDelete = { productToDelete = it },
                                onGenerateBarcode = { 
                                    try {
                                        val encodedId = URLEncoder.encode(product.id, StandardCharsets.UTF_8.toString())
                                        val encodedName = URLEncoder.encode(product.name, StandardCharsets.UTF_8.toString())
                                        navController.navigate("barcode/$encodedId/$encodedName")
                                    } catch (e: Exception) {
                                        toast.show("Error al abrir visor: ${e.message}", ToastType.ERROR)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = FocusBlue, contentColor = Color.White, shape = CircleShape, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp).navigationBarsPadding()) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }

        if (showAddDialog) {
            ProductFormDialog(title = "Nuevo Producto", onDismiss = { showAddDialog = false }, onConfirm = { n, p, q, d -> productViewModel.addProduct(n, p, q, d) })
        }

        if (productToEdit != null) {
            ProductFormDialog(title = "Editar Producto", product = productToEdit, onDismiss = { productToEdit = null }, onConfirm = { n, p, q, d ->
                productViewModel.updateProduct(productToEdit!!.copy(name = n, price = p, quantity = q, expiryDate = d), productToEdit!!.quantity)
            })
        }

        if (productToDelete != null && isAdmin) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Estás seguro de que deseas eliminar '${productToDelete?.name}'? Esta acción es definitiva.") },
                confirmButton = {
                    Button(onClick = { productViewModel.deleteProduct(productToDelete!!.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Eliminar", color = Color.White)
                    }
                },
                dismissButton = { TextButton(onClick = { productToDelete = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun ProductCard(product: Product, isAdmin: Boolean, onEdit: (Product) -> Unit, onDelete: (Product) -> Unit, onGenerateBarcode: (Product) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val priceColor = Color(0xFF4CAF50)
    val accentColor = FocusBlue
    val bgColor = accentColor.copy(alpha = 0.05f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).background(accentColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("C$ ${String.format("%.2f", product.price)}", color = priceColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 4.dp)) {
                        Text("STOCK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${product.quantity}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (product.quantity <= 5) Color.Red else MaterialTheme.colorScheme.onSurface)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Editar") }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }, onClick = { showMenu = false; onEdit(product) })
                            DropdownMenuItem(text = { Text("Código de Barras") }, leadingIcon = { Icon(Icons.Default.QrCode, null, modifier = Modifier.size(18.dp)) }, onClick = { showMenu = false; onGenerateBarcode(product) })
                            if (isAdmin) {
                                DropdownMenuItem(text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }, onClick = { showMenu = false; onDelete(product) })
                            }
                        }
                    }
                }
            }
            if (!product.expiryDate.isNullOrEmpty()) {
                Text(text = product.expiryDate!!, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Normal, modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 1.dp, end = 66.dp))
            }
        }
    }
}

@Composable
fun ProductFormDialog(title: String, product: Product? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int, String?) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var expiryDate by remember { mutableStateOf(product?.expiryDate ?: "") }
    val context = LocalContext.current
    
    val nameFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }
    val qtyFocus = remember { FocusRequester() }
    val expiryFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { nameFocus.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { 
                    val p = price.toDoubleOrNull() ?: 0.0
                    val q = quantity.toIntOrNull() ?: 0
                    val formattedName = name.trim().lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    if (name.isNotEmpty()) onConfirm(formattedName, p, q, expiryDate.ifEmpty { null }) 
                }, 
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = FocusBlue, contentColor = Color.White)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GUARDAR PRODUCTO", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).background(FocusBlue.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Inventory, null, tint = FocusBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nombre del Producto") }, 
                    leadingIcon = { Icon(Icons.Default.Tag, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(nameFocus), 
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true, 
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Words
                    ), 
                    keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )

                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Precio C$") }, 
                    leadingIcon = { Icon(Icons.Default.Payments, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(priceFocus), 
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), 
                    keyboardActions = KeyboardActions(onNext = { qtyFocus.requestFocus() }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )

                OutlinedTextField(
                    value = quantity, 
                    onValueChange = { quantity = it }, 
                    label = { Text("Stock") }, 
                    leadingIcon = { Icon(Icons.Default.Numbers, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(qtyFocus), 
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), 
                    keyboardActions = KeyboardActions(onNext = { expiryFocus.requestFocus() }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )

                OutlinedTextField(
                    value = expiryDate, 
                    onValueChange = {}, 
                    label = { Text("Vencimiento (Opcional)") }, 
                    readOnly = true, 
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) }, 
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(context, { _, y, m, d -> expiryDate = String.format("%02d/%02d/%d", d, m + 1, y) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }) { Icon(Icons.Default.CalendarMonth, null, tint = FocusBlue) }
                    }, 
                    modifier = Modifier.fillMaxWidth().focusRequester(expiryFocus),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )
            }
        }
    )
}
