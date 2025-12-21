package com.spectrum.phoenix.logic.model

data class Sale(
    val id: String = "",
    val clientId: String? = null, // null si es venta general
    val clientName: String = "Venta General",
    val items: List<SaleItem> = emptyList(),
    val total: Double = 0.0,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sellerId: String = "",
    val sellerName: String = ""
)

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val subtotal: Double = 0.0
)
