package com.spectrum.phoenix.logic.model

data class ActivityLog(
    val id: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "", // dd/MM/yyyy
    val time: String = "",  // hh:mm:ss a
    val userName: String = "Sistema"
)
