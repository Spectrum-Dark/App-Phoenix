package com.spectrum.phoenix.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.ui.main.clientes.creditos.CreditosScreen
import com.spectrum.phoenix.ui.main.clientes.lista.ListaClientesScreen
import com.spectrum.phoenix.ui.main.dashboard.DashboardScreen
import com.spectrum.phoenix.ui.main.perfil.PerfilScreen
import com.spectrum.phoenix.ui.main.productos.almacen.AlmacenScreen
import com.spectrum.phoenix.ui.main.productos.vencimiento.VencimientoScreen
import com.spectrum.phoenix.ui.main.reportes.ReportesScreen
import com.spectrum.phoenix.ui.main.ventas.VentasScreen
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme
import kotlinx.coroutines.launch

data class MenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val children: List<MenuItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, userName: String = "Usuario") {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
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
            MenuItem("lista", "Afiliados", Icons.AutoMirrored.Filled.List), // Nombre cambiado aquí
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

    PhoenixTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            "Phoenix App",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FocusBlue,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 16.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            menuItems.forEach { item ->
                                if (item.children.isEmpty()) {
                                    NavigationDrawerItem(
                                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                                        label = { Text(item.title, fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal) },
                                        selected = item == selectedItem,
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = FocusBlue.copy(alpha = 0.1f),
                                            selectedIconColor = FocusBlue,
                                            selectedTextColor = FocusBlue,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            selectedItem = item
                                            mainContentNavController.navigate(item.id) {
                                                popUpTo(mainContentNavController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        },
                                        modifier = Modifier.height(48.dp).padding(vertical = 2.dp)
                                    )
                                } else {
                                    val isExpanded = expandedItems.contains(item.id)
                                    NavigationDrawerItem(
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(item.title, modifier = Modifier.weight(1f))
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                                        selected = false,
                                        onClick = {
                                            expandedItems = if (isExpanded) expandedItems - item.id else expandedItems + item.id
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.height(48.dp).padding(vertical = 2.dp)
                                    )

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(modifier = Modifier.padding(start = 16.dp)) {
                                            item.children.forEach { child ->
                                                NavigationDrawerItem(
                                                    icon = { Icon(child.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                    label = { Text(child.title, fontSize = 14.sp, fontWeight = if (child == selectedItem) FontWeight.Bold else FontWeight.Normal) },
                                                    selected = child == selectedItem,
                                                    colors = NavigationDrawerItemDefaults.colors(
                                                        selectedContainerColor = FocusBlue.copy(alpha = 0.1f),
                                                        selectedIconColor = FocusBlue,
                                                        selectedTextColor = FocusBlue
                                                    ),
                                                    onClick = {
                                                        scope.launch { drawerState.close() }
                                                        selectedItem = child
                                                        mainContentNavController.navigate(child.id) {
                                                            popUpTo(mainContentNavController.graph.startDestinationId)
                                                            launchSingleTop = true
                                                        }
                                                    },
                                                    modifier = Modifier.height(40.dp).padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Box(modifier = Modifier.padding(bottom = 16.dp)) {
                            NavigationDrawerItem(
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userName,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
                                    }
                                },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(FocusBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                },
                                selected = false,
                                onClick = { showUserMenu = !showUserMenu },
                                modifier = Modifier.height(56.dp).padding(vertical = 2.dp)
                            )

                            DropdownMenu(
                                expanded = showUserMenu,
                                onDismissRequest = { showUserMenu = false },
                                modifier = Modifier.width(180.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ver Perfil") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showUserMenu = false
                                        scope.launch { drawerState.close() }
                                        selectedItem = perfilMenuItem
                                        mainContentNavController.navigate("perfil")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showUserMenu = false
                                        scope.launch { drawerState.close() }
                                        // LIMPIAR SESION AL CERRAR
                                        sessionManager.clearSession()
                                        navController.navigate("login") {
                                            popUpTo("main/{userName}") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = selectedItem?.title ?: "", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = null)
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
}
