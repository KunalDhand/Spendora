package com.example.testing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.repository.WalletRepository
import com.example.testing.data.local.WalletEntity
import com.example.testing.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

import com.example.testing.domain.model.WalletType

class WalletViewModel(
    private val repository: WalletRepository
) : ViewModel() {

    val wallets: Flow<List<WalletEntity>> = repository.getAllWallets()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    fun addWallet(name: String, type: WalletType, balance: Double? = 0.0) {
        viewModelScope.launch {
            val result = repository.insert(
                WalletEntity(
                    name = name,
                    type = type.name,
                    balance = balance ?: 0.0
                )
            )
            if (result == -1L) {
                _error.emit("Wallet with this name and type already exists")
            }
        }
    }

    fun transferMoney(fromId: Int, toId: Int, amount: Double) {
        if (fromId == toId) return
        viewModelScope.launch {
            val transaction = TransactionEntity(
                amount = amount,
                walletId = fromId,
                toWalletId = toId,
                type = "TRANSFER",
                timestamp = System.currentTimeMillis(),
                categoryId = null,
                note = "Transfer",
                personId = null
            )
            repository.insertTransfer(transaction)
            repository.updateBalance(fromId, -amount)
            repository.updateBalance(toId, amount)
        }
    }

    fun deleteWallet(wallet: WalletEntity, onResult: (String) -> Unit) {
        if (wallet.isDefault) {
            onResult("Default wallet cannot be deleted")
            return
        }
        viewModelScope.launch {
            val count = repository.getTransactionCountForWallet(wallet.id)
            if (count > 0) {
                onResult("Cannot delete wallet with transactions")
            } else {
                repository.delete(wallet)
                onResult("Wallet deleted")
            }
        }
    }

    fun setCashOpeningBalance(amount: Double) {
        viewModelScope.launch {
            val cashWallet = repository.getWalletByName("Cash")
            cashWallet?.let {
                repository.updateBalance(it.id, amount)
            }
        }
    }
}
