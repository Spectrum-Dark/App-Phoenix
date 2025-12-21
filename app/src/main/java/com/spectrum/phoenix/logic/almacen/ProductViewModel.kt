package com.spectrum.phoenix.logic.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Usamos WhileSubscribed para que no trabaje si la vista no existe, 
    // pero guarde el estado 5 segundos por si es solo una rotación o navegación rápida.
    val products: StateFlow<List<Product>> = repository.getProducts()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(_searchQuery, products) { query, list ->
        if (query.isEmpty()) {
            list
        } else {
            // Filtro en hilo secundario para no trabar la UI
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                expiryDate = expiryDate,
                timestamp = System.currentTimeMillis()
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
