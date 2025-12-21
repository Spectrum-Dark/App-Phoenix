package com.spectrum.phoenix.logic.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.almacen.ProductRepository
import com.spectrum.phoenix.logic.clientes.ClientRepository
import com.spectrum.phoenix.logic.clientes.CreditRepository
import com.spectrum.phoenix.logic.model.ActivityLog
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.ventas.SaleRepository
import kotlinx.coroutines.flow.*

class DashboardViewModel : ViewModel() {
    private val productRepo = ProductRepository()
    private val clientRepo = ClientRepository()
    private val creditRepo = CreditRepository()
    private val saleRepo = SaleRepository()
    private val logRepo = ActivityLogRepository()

    // Resumen de Ganancias
    val ventasGenerales: StateFlow<Double> = saleRepo.getSales()
        .map { list -> list.filter { it.clientId == null }.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCreditos: StateFlow<Double> = creditRepo.getCredits()
        .map { list -> list.sumOf { it.totalDebt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Inventario
    val totalProductosRegistrados: StateFlow<Int> = productRepo.getProducts()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStockFisico: StateFlow<Int> = productRepo.getProducts()
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // LÓGICA DE STOCK BAJO (Paso 2)
    val lowStockProducts: StateFlow<List<Product>> = productRepo.getProducts()
        .map { list -> list.filter { it.quantity <= 5 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Clientes
    val totalClientes: StateFlow<Int> = clientRepo.getClients()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Historial de Actividad
    val activityLogs: StateFlow<List<ActivityLog>> = logRepo.getLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
