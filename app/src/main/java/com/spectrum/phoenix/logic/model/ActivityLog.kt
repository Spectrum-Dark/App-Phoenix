package com.spectrum.phoenix.logic.model

data class ActivityLog(
    val id: String = "",
    val action: String = "", // e.g., "Producto Agregado", "Venta Realizada", etc.
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "" // Formato dd/MM/yyyy hh:mm:ss a
)
