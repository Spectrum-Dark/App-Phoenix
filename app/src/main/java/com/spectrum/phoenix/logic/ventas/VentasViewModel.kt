package com.spectrum.phoenix.logic.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.almacen.ProductRepository
import com.spectrum.phoenix.logic.clientes.ClientRepository
import com.spectrum.phoenix.logic.model.Client
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.model.Sale
import com.spectrum.phoenix.logic.model.SaleItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VentasViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val clientRepository = ClientRepository()
    private val saleRepository = SaleRepository()

    val allProducts: StateFlow<List<Product>> = productRepository.getProducts()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allClients: StateFlow<List<Client>> = clientRepository.getClients()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery

    val filteredProducts: StateFlow<List<Product>> = _productSearchQuery
        .combine(allProducts) { query, products ->
            if (query.isEmpty()) emptyList()
            else products.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cartItems = MutableStateFlow<List<SaleItem>>(emptyList())
    val cartItems: StateFlow<List<SaleItem>> = _cartItems

    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient

    private val _saleResult = MutableStateFlow<Result<Unit>?>(null)
    val saleResult: StateFlow<Result<Unit>?> = _saleResult

    val total: StateFlow<Double> = _cartItems.map { items -> 
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onProductSearchQueryChange(query: String) {
        _productSearchQuery.value = query
    }

    fun selectClient(client: Client?) {
        _selectedClient.value = client
    }

    fun addToCart(product: Product, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == product.id }

        if (existingIndex != -1) {
            val existing = currentItems[existingIndex]
            val newQty = existing.quantity + quantity
            currentItems[existingIndex] = existing.copy(
                quantity = newQty,
                subtotal = newQty * existing.price
            )
        } else {
            currentItems.add(SaleItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                quantity = quantity,
                subtotal = quantity * product.price
            ))
        }
        _cartItems.value = currentItems
        _productSearchQuery.value = "" 
    }

    fun removeFromCart(item: SaleItem) {
        _cartItems.value = _cartItems.value.filter { it.productId != item.productId }
    }

    fun finalizeSale() {
        if (_cartItems.value.isEmpty()) return
        viewModelScope.launch {
            try {
                val sale = Sale(
                    clientId = _selectedClient.value?.id,
                    clientName = _selectedClient.value?.let { "${it.name} ${it.lastName}" } ?: "Venta General",
                    items = _cartItems.value,
                    total = _cartItems.value.sumOf { it.subtotal }
                )
                val result = saleRepository.registerSale(sale)
                if (result.isSuccess) {
                    _cartItems.value = emptyList()
                    _selectedClient.value = null
                }
                _saleResult.value = result
            } catch (e: Exception) {
                _saleResult.value = Result.failure(e)
            }
        }
    }

    fun clearResult() { _saleResult.value = null }
}
