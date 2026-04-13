package com.example.testing.ui.viewmodel

data class TransactionUI(
    val id: Int,
    val amount: Double,
    val type: String,
    val date: String,
    val category: String,
    val wallet: String,
    val person: String?,
    val tags: List<String>,
    val note: String?
)
