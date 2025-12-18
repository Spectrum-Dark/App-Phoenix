package com.spectrum.phoenix.logic.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Entry(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val date: String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
    val timestamp: Long = System.currentTimeMillis()
)
