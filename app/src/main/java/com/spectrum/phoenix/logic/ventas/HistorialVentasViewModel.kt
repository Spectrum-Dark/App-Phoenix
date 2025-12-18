package com.spectrum.phoenix.logic.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.Sale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistorialVentasViewModel : ViewModel() {
    private val repository = SaleRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allSales = repository.getSales()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSales: StateFlow<List<Sale>> = _searchQuery
        .combine(_allSales) { query, list ->
            if (query.isEmpty()) list
            else list.filter { it.clientName.contains(query, ignoreCase = true) || it.date.contains(query) }
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

    fun clearResult() {
        _opResult.value = null
    }
}
