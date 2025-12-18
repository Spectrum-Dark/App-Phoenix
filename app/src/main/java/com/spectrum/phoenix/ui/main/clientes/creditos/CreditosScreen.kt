package com.spectrum.phoenix.ui.main.clientes.creditos

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.clientes.CreditosViewModel
import com.spectrum.phoenix.logic.model.Credit
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditosScreen(creditosViewModel: CreditosViewModel = viewModel()) {
    val credits by creditosViewModel.filteredCredits.collectAsStateWithLifecycle()
    val searchQuery by creditosViewModel.searchQuery.collectAsStateWithLifecycle()
    val result by creditosViewModel.opResult.collectAsStateWithLifecycle()
    val toast = LocalToastController.current 
    
    var selectedCreditForDetails by remember { mutableStateOf<Credit?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                toast.show("Operación exitosa", ToastType.SUCCESS)
                creditosViewModel.clearResult()
            } else {
                toast.show(it.exceptionOrNull()?.message ?: "Error", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { creditosViewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar deudor...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = FocusBlue) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue)
            )

            if (credits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin saldo pendiente", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("ESTADO DE CUENTAS ACTIVO", style = MaterialTheme.typography.labelLarge, color = FocusBlue, fontWeight = FontWeight.Black)
                            IconButton(onClick = { showClearAllConfirm = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DeleteSweep, null, tint = Color.Red)
                            }
                        }
                    }
                    itemsIndexed(credits) { _, credit ->
                        CreditCard(credit = credit, onDetails = { selectedCreditForDetails = credit })
                    }
                }
            }
        }

        if (showClearAllConfirm) {
            AlertDialog(
                onDismissRequest = { showClearAllConfirm = false },
                title = { Text("¿Liquidar Todas las Cuentas?") },
                text = { Text("Se borrarán los saldos de todos los deudores definitivamente. ¿Confirmar?") },
                confirmButton = {
                    Button(onClick = { 
                        creditosViewModel.clearAllCredits()
                        showClearAllConfirm = false 
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("Confirmar Liquidación", color = Color.White)
                    }
                },
                dismissButton = { TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancelar") } }
            )
        }

        if (selectedCreditForDetails != null) {
            CreditDetailsDialog(credit = selectedCreditForDetails!!, onDismiss = { selectedCreditForDetails = null })
        }
    }
}

@Composable
fun CreditCard(credit: Credit, onDetails: () -> Unit) {
    val priceGreen = Color(0xFF4CAF50)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(credit.clientName.take(1).uppercase(), color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(credit.clientName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Act: ${credit.lastUpdate.take(10)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("DEUDA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("C$ ${String.format("%.2f", credit.totalDebt)}", color = priceGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.size(24.dp).background(priceGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = priceGreen, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditDetailsDialog(credit: Credit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = FocusBlue)
                    Spacer(modifier = Modifier.width(10.dp)); Text("Movimientos", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Text(credit.clientName, color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(credit.history.reversed()) { _, movement ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = movement.type, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(movement.type == "CARGO") Color.Red else Color(0xFF4CAF50))
                                    Text(movement.description, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(movement.fullDate, fontSize = 10.sp, color = Color.Gray)
                                }
                                Text(text = "C$ ${String.format("%.2f", movement.amount)}", fontWeight = FontWeight.Black, color = if(movement.type == "CARGO") Color.Red else Color(0xFF4CAF50))
                            }
                            if (movement.type == "CARGO" && movement.items.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 8.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(8.dp).fillMaxWidth()) {
                                    movement.items.forEach { item ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${item.quantity}x ${item.productName}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text("C$ ${String.format("%.2f", item.subtotal)}", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp)
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FocusBlue)) {
                    Text("CERRAR DETALLE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
