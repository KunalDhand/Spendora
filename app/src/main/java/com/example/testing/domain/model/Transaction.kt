package com.example.testing.domain.model

data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val walletId: Int,
    val categoryId: Int,
    val type: String, // EXPENSE or INCOME
    val note: String?,
    val timestamp: Long
)
