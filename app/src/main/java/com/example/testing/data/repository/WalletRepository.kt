package com.example.testing.data.repository

import com.example.testing.data.local.TransactionDao
import com.example.testing.data.local.TransactionEntity
import com.example.testing.data.local.WalletDao
import com.example.testing.data.local.WalletEntity
import kotlinx.coroutines.flow.Flow

class WalletRepository(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao
) {
    fun getAllWallets(): Flow<List<WalletEntity>> = walletDao.getAllWallets()

    suspend fun getAllWalletsOnce(): List<WalletEntity> = walletDao.getAllWalletsOnce()

    suspend fun insert(wallet: WalletEntity): Long = walletDao.insert(wallet)

    suspend fun update(wallet: WalletEntity) = walletDao.update(wallet)

    suspend fun delete(wallet: WalletEntity) = walletDao.delete(wallet)

    suspend fun updateBalance(walletId: Int, amount: Double) = walletDao.updateBalance(walletId, amount)

    suspend fun insertTransfer(transaction: TransactionEntity) = transactionDao.insert(transaction)

    suspend fun getTransactionCountForWallet(walletId: Int): Int = 
        transactionDao.getTransactionCountForWallet(walletId)

    suspend fun getWalletByName(name: String): WalletEntity? = walletDao.getWalletByName(name)
}
