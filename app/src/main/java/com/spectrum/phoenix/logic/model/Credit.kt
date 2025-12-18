package com.spectrum.phoenix.logic.model

data class Credit(
    val id: String = "", 
    val clientId: String = "",
    val clientName: String = "",
    val totalDebt: Double = 0.0,
    val lastUpdate: String = "",
    val history: List<CreditMovement> = emptyList()
)

data class CreditMovement(
    val id: String = "",
    val date: String = "", // Formato dd/MM/yyyy
    val fullDate: String = "", // Formato dd/MM/yyyy HH:mm:ss
    val amount: Double = 0.0,
    val type: String = "CARGO", // "CARGO" o "ABONO"
    val description: String = "",
    val items: List<SaleItem> = emptyList() // Productos comprados en este cargo
)
