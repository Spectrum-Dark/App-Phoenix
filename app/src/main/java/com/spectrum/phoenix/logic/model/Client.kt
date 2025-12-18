package com.spectrum.phoenix.logic.model

data class Client(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val registrationDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
