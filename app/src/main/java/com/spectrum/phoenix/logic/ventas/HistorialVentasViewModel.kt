package com.spectrum.phoenix.logic.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.PhoenixApp
import com.spectrum.phoenix.logic.model.Sale
import com.spectrum.phoenix.logic.session.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistorialVentasViewModel : ViewModel() {
    private val repository = SaleRepository()
    private val sessionManager = SessionManager(PhoenixApp.context)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allSales = repository.getSales()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSales: StateFlow<List<Sale>> = _searchQuery
        .combine(_allSales) { query, list ->
            // FILTRADO POR ROL Y NOMBRE DE VENDEDOR
            val role = sessionManager.getUserRole()
            val userId = sessionManager.getUserId() ?: ""
            
            // Si es admin ve todo, si es usuario solo ve sus propias ventas
            val baseList = if (role == "admin") {
                list
            } else {
                list.filter { it.sellerId == userId }
            }

            if (query.isEmpty()) baseList
            else baseList.filter { 
                it.clientName.contains(query, ignoreCase = true) || 
                it.date.contains(query) ||
                it.sellerName.contains(query, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _opResult = MutableStateFlow<Result<Unit>?>(null)
    val opResult: StateFlow<Result<Unit>?> = _opResult

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun revertirVenta(sale: Sale) {
        viewModelScope.launch {
            _opResult.value = repository.revertSale(sale)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            _opResult.value = repository.clearAllSales()
        }
    }

    fun clearResult() {
        _opResult.value = null
    }
}
