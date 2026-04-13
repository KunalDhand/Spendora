package com.example.testing.domain.model

data class Wallet(
    val id: Int = 0,
    val name: String,
    val type: String, // CASH / BANK / UPI / CRYPTO
    val balance: Double
)
