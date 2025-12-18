package com.spectrum.phoenix.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.spectrum.phoenix.ui.main.configuracion.BackupScreen
import com.spectrum.phoenix.ui.main.productos.almacen.AlmacenScreen
import com.spectrum.phoenix.ui.main.productos.vencimiento.VencimientoScreen
import com.spectrum.phoenix.ui.main.reportes.ReportesScreen
import com.spectrum.phoenix.ui.main.ventas.VentasScreen
import com.spectrum.phoenix.ui.main.ventas.historial.HistorialVentasScreen
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
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // ESTADO DEL NOMBRE EN TIEMPO REAL
    var currentUserName by remember { mutableStateOf(sessionManager.getUserName() ?: "Usuario") }

    val adminPanel = MenuItem("admin_panel", "Panel Administrativo", Icons.Default.AdminPanelSettings, children = listOf(
        MenuItem("dashboard", "Dashboard Principal", Icons.Default.Dashboard),
        MenuItem("ventas", "Punto de Venta", Icons.Default.PointOfSale),
        MenuItem("historial_ventas", "Registro de Operaciones", Icons.Default.History),
        MenuItem("reportes", "Centro de Inteligencia", Icons.Default.Assessment)
    ))

    val configPanel = MenuItem("configuracion", "Ajustes y Configuración", Icons.Default.Settings, children = listOf(
        MenuItem("perfil", "Mi Perfil de Usuario", Icons.Default.Person),
        MenuItem("backup", "Respaldo de Datos", Icons.Default.CloudUpload)
    ))

    val menuItems = listOf(
        adminPanel,
        MenuItem("productos", "Gestión de Inventario", Icons.Default.Inventory, children = listOf(
            MenuItem("almacen", "Control de Stock", Icons.Default.Warehouse),
            MenuItem("vencimiento", "Control de Caducidad", Icons.Default.Event)
        )),
        MenuItem("clientes", "Cartera de Clientes", Icons.Default.People, children = listOf(
            MenuItem("lista", "Afiliados Registrados", Icons.AutoMirrored.Filled.List),
            MenuItem("creditos", "Estado de Cuentas", Icons.Default.CreditCard)
        )),
        configPanel
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainContentNavController = rememberNavController()

    var selectedItem by remember { mutableStateOf<MenuItem?>(adminPanel.children[0]) }
    var expandedItems by remember { mutableStateOf(setOf("admin_panel")) }

    // Efecto para actualizar el nombre del menú cada vez que se navega (por si cambió en el Perfil)
    LaunchedEffect(mainContentNavController.currentBackStackEntry) {
        currentUserName = sessionManager.getUserName() ?: "Usuario"
    }

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
                            "Phoenix Enterprise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
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
                                        label = { Text(item.title, fontWeight = if (item == selectedItem) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 14.sp) },
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
                                                Text(item.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                                    label = { Text(child.title, fontSize = 13.sp, fontWeight = if (child == selectedItem) FontWeight.Bold else FontWeight.Normal) },
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp, start = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(FocusBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = currentUserName.take(1).uppercase(), color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = currentUserName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    sessionManager.clearSession()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            content = {
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
                ) { innerPadding ->
                    NavHost(
                        navController = mainContentNavController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardScreen() }
                        composable("ventas") { VentasScreen() }
                        composable("historial_ventas") { HistorialVentasScreen() }
                        composable("reportes") { ReportesScreen() }
                        composable("almacen") { AlmacenScreen() }
                        composable("vencimiento") { VencimientoScreen() }
                        composable("lista") { ListaClientesScreen() }
                        composable("creditos") { CreditosScreen() }
                        composable("perfil") { PerfilScreen() }
                        composable("backup") { BackupScreen() }
                    }
                }
            }
        )
    }
}
