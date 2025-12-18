package com.spectrum.phoenix.ui.main.clientes.lista

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.clientes.ClientViewModel
import com.spectrum.phoenix.logic.model.Client
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(clientViewModel: ClientViewModel = viewModel()) {
    val clients by clientViewModel.filteredClients.collectAsStateWithLifecycle()
    val searchQuery by clientViewModel.searchQuery.collectAsStateWithLifecycle()
    val result by clientViewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }
    var clientToDelete by remember { mutableStateOf<Client?>(null) }

    val isDialogOpen = showAddDialog || clientToEdit != null

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Operación exitosa", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                clientToEdit = null
                clientToDelete = null
                clientViewModel.clearResult()
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
                // Buscador con bloqueo de foco
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { clientViewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar cliente...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FocusBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { clientViewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .focusProperties { canFocus = !isDialogOpen },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                if (clients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isEmpty()) "No hay clientes registrados" else "No se encontraron resultados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
                    ) {
                        item {
                            Text(
                                "${clients.size} clientes en total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(clients, key = { it.id }) { client ->
                            ClientCard(
                                client = client,
                                onEdit = { clientToEdit = it },
                                onDelete = { clientToDelete = it }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FocusBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .navigationBarsPadding()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Añadir Cliente")
            }
        }

        if (showAddDialog) {
            ClientFormDialog(
                title = "Nuevo Cliente",
                onDismiss = { showAddDialog = false },
                onConfirm = { name, lastName ->
                    clientViewModel.addClient(name, lastName)
                }
            )
        }

        if (clientToEdit != null) {
            ClientFormDialog(
                title = "Editar Cliente",
                client = clientToEdit,
                onDismiss = { clientToEdit = null },
                onConfirm = { name, lastName ->
                    clientViewModel.updateClient(clientToEdit!!.copy(name = name, lastName = lastName))
                }
            )
        }

        if (clientToDelete != null) {
            AlertDialog(
                onDismissRequest = { clientToDelete = null },
                title = { Text("Eliminar Cliente") },
                text = { Text("¿Estás seguro de que deseas eliminar a '${clientToDelete?.name} ${clientToDelete?.lastName}'?") },
                confirmButton = {
                    Button(
                        onClick = { clientViewModel.deleteClient(clientToDelete!!.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { clientToDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun ClientCard(client: Client, onEdit: (Client) -> Unit, onDelete: (Client) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val accentColor = FocusBlue
    val bgColor = accentColor.copy(alpha = 0.05f)

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
                Text(
                    text = client.name.take(1).uppercase() + client.lastName.take(1).uppercase(),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${client.name} ${client.lastName}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Registrado: ${client.registrationDate}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onEdit(client)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onDelete(client)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(
    title: String,
    client: Client? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(client?.name ?: "") }
    var lastName by remember { mutableStateOf(client?.lastName ?: "") }
    
    val nameFocus = remember { FocusRequester() }
    val lastNameFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { nameFocus.requestFocus() }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight().imePadding(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).background(FocusBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = FocusBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.replace("\n", "") },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(nameFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { lastNameFocus.requestFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it.replace("\n", "") },
                    label = { Text("Apellido") },
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = FocusBlue, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(lastNameFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        if (name.isNotEmpty() && lastName.isNotEmpty()) onConfirm(name, lastName)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FocusBlue, focusedLabelColor = FocusBlue)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && lastName.isNotEmpty()) onConfirm(name, lastName)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR CLIENTE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
