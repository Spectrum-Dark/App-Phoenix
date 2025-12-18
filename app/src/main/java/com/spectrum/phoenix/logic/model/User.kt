package com.spectrum.phoenix.logic.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "user"
)