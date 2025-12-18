package com.spectrum.phoenix.logic.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(_searchQuery, products) { query, list ->
        if (query.isEmpty()) {
            list
        } else {
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _result = MutableStateFlow<Result<Unit>?>(null)
    val result: StateFlow<Result<Unit>?> = _result

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery.replace("\n", "")
    }

    fun addProduct(name: String, price: Double, quantity: Int, expiryDate: String?) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price,
                quantity = quantity,
                expiryDate = expiryDate
            )
            _result.value = repository.addProduct(product)
        }
    }

    fun updateProduct(product: Product, oldQuantity: Int) {
        viewModelScope.launch {
            _result.value = repository.updateProduct(product, oldQuantity)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _result.value = repository.deleteProduct(productId)
        }
    }

    fun clearResult() {
        _result.value = null
    }
}
