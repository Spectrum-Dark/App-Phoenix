package com.spectrum.phoenix.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spectrum.phoenix.ui.main.clientes.creditos.CreditosScreen
import com.spectrum.phoenix.ui.main.clientes.lista.ListaClientesScreen
import com.spectrum.phoenix.ui.main.dashboard.DashboardScreen
import com.spectrum.phoenix.ui.main.perfil.PerfilScreen
import com.spectrum.phoenix.ui.main.productos.almacen.AlmacenScreen
import com.spectrum.phoenix.ui.main.productos.vencimiento.VencimientoScreen
import com.spectrum.phoenix.ui.main.reportes.ReportesScreen
import com.spectrum.phoenix.ui.main.ventas.VentasScreen
import kotlinx.coroutines.launch

data class MenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val children: List<MenuItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val adminPanel = MenuItem("admin_panel", "Panel administrativo", Icons.Default.AdminPanelSettings, children = listOf(
        MenuItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        MenuItem("ventas", "Ventas", Icons.Default.PointOfSale),
        MenuItem("reportes", "Reportes", Icons.Default.Assessment)
    ))

    val menuItems = listOf(
        adminPanel,
        MenuItem("productos", "Productos", Icons.Default.Inventory, children = listOf(
            MenuItem("almacen", "Almacen", Icons.Default.Warehouse),
            MenuItem("vencimiento", "Vencimiento", Icons.Default.Event)
        )),
        MenuItem("clientes", "Clientes", Icons.Default.People, children = listOf(
            MenuItem("lista", "Lista", Icons.Default.List),
            MenuItem("creditos", "Creditos", Icons.Default.CreditCard)
        ))
    )

    val perfilMenuItem = MenuItem("perfil", "Perfil", Icons.Default.Person)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainContentNavController = rememberNavController()

    var selectedItem by remember { mutableStateOf<MenuItem?>(adminPanel.children[0]) }
    var expandedItems by remember { mutableStateOf(setOf("admin_panel")) }
    var showUserMenu by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(12.dp)) {
                    menuItems.forEach { item ->
                        if (item.children.isEmpty()) {
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = item == selectedItem,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    selectedItem = item
                                    mainContentNavController.navigate(item.id) {
                                        popUpTo(mainContentNavController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        } else {
                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.title, modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = if (expandedItems.contains(item.id)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand"
                                        )
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                selected = false,
                                onClick = {
                                    expandedItems = if (expandedItems.contains(item.id)) expandedItems - item.id else expandedItems + item.id
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            AnimatedVisibility(visible = expandedItems.contains(item.id)) {
                                Column(modifier = Modifier.padding(start = 24.dp)) {
                                    item.children.forEach { child ->
                                        NavigationDrawerItem(
                                            icon = { Icon(child.icon, contentDescription = child.title) },
                                            label = { Text(child.title) },
                                            selected = child == selectedItem,
                                            onClick = {
                                                scope.launch { drawerState.close() }
                                                selectedItem = child
                                                mainContentNavController.navigate(child.id) {
                                                    popUpTo(mainContentNavController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            },
                                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    selectedItem = perfilMenuItem
                                    mainContentNavController.navigate(perfilMenuItem.id) {
                                        popUpTo(mainContentNavController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                    showUserMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión") },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                    showUserMenu = false
                                }
                            )
                        }

                        NavigationDrawerItem(
                            label = {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Admin", modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                }
                            },
                            icon = {
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A", color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            selected = false,
                            onClick = { showUserMenu = !showUserMenu },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = selectedItem?.title ?: "") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = mainContentNavController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding)
            ) {
                composable("dashboard") { DashboardScreen() }
                composable("ventas") { VentasScreen() }
                composable("reportes") { ReportesScreen() }
                composable("almacen") { AlmacenScreen() }
                composable("vencimiento") { VencimientoScreen() }
                composable("lista") { ListaClientesScreen() }
                composable("creditos") { CreditosScreen() }
                composable("perfil") { PerfilScreen() }
            }
        }
    }
}