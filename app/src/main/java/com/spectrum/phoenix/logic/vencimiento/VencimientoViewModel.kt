package com.spectrum.phoenix.logic.vencimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.almacen.ProductRepository
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VencimientoViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val expiringProducts: StateFlow<List<Product>> = combine(_searchQuery, products) { query, list ->
        val filteredByDate = list.filter { product ->
            product.expiryDate?.let { dateStr ->
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val expiryDate = sdf.parse(dateStr)
                    expiryDate?.let { isExpiringSoon(it) } ?: false
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }

        if (query.isEmpty()) {
            filteredByDate
        } else {
            filteredByDate.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery.replace("\n", "")
    }

    private fun isExpiringSoon(expiryDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Añadimos 3 días a la fecha actual
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val limitDate = calendar.time
        
        // Es pronto si la fecha de vencimiento es antes o igual a hoy + 3 días
        // Y también si ya venció (expiryDate < today)
        return expiryDate.before(limitDate) || SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(expiryDate) == SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(limitDate)
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _deleteResult.value = repository.deleteProduct(productId)
        }
    }

    fun deleteAllVisible() {
        viewModelScope.launch {
            val listToDelete = expiringProducts.value
            var allSuccess = true
            listToDelete.forEach {
                val result = repository.deleteProduct(it.id)
                if (result.isFailure) allSuccess = false
            }
            _deleteResult.value = if (allSuccess) Result.success(Unit) else Result.failure(Exception("Algunos productos no se pudieron eliminar"))
        }
    }

    fun clearResult() {
        _deleteResult.value = null
    }
}
