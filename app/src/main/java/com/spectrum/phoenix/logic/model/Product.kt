package com.spectrum.phoenix.logic.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val expiryDate: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
