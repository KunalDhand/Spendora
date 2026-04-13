package com.example.testing.ui.viewmodel

data class TransactionUI(
    val id: Int,
    val amount: Double,
    val type: String,
    val date: String,
    val time: String,
    val category: String,
    val wallet: String,
    val person: String?,
    val personId: Int?,
    val categoryId: Int?,
    val walletId: Int,
    val toWallet: String? = null,
    val toWalletId: Int? = null,
    val tags: List<String>,
    val tagIds: List<Int>,
    val note: String?
)
