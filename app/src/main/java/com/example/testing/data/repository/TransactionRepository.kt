package com.example.testing.data.repository

import com.example.testing.data.local.TransactionDao
import com.example.testing.data.local.TransactionEntity
import com.example.testing.data.local.CategorySummary
import kotlinx.coroutines.flow.Flow

import com.example.testing.data.local.CategoryDao
import com.example.testing.data.local.TagDao
import com.example.testing.data.local.TransactionTagCrossRef
import com.example.testing.data.local.WalletDao
import com.example.testing.data.local.CategoryEntity
import com.example.testing.data.local.WalletEntity
import com.example.testing.data.local.NetData
import com.example.testing.data.local.PersonDao
import com.example.testing.data.local.PersonCreditDao
import com.example.testing.data.local.PersonCreditEntity

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val walletDao: WalletDao,
    private val tagDao: TagDao,
    private val personDao: PersonDao,
    private val personCreditDao: PersonCreditDao
) {
    suspend fun getAllCategoriesOnce(): List<CategoryEntity> = categoryDao.getAllCategoriesOnce()
    suspend fun getAllWalletsOnce(): List<WalletEntity> = walletDao.getAllWalletsOnce()
    suspend fun getAllPersonsOnce(): List<com.example.testing.data.local.PersonEntity> = personDao.getAllPersonsOnce()

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun getTagsForTransactionOnce(transactionId: Int): List<com.example.testing.data.local.TagEntity> = 
        tagDao.getTagsForTransactionOnce(transactionId)

    suspend fun getTransactionById(id: Int): TransactionEntity? = transactionDao.getTransactionById(id)

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun addTagToTransaction(transactionId: Long, tagId: Int) {
        tagDao.insertCrossRef(TransactionTagCrossRef(transactionId.toInt(), tagId))
    }

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    fun getTodayIncome(): Flow<Double?> = transactionDao.getTodayIncome()
    fun getTodayExpense(): Flow<Double?> = transactionDao.getTodayExpense()
    fun getTodayNet(): Flow<Double?> = transactionDao.getTodayNet()

    fun getMonthlyIncome(): Flow<Double?> = transactionDao.getMonthlyIncome()
    fun getMonthlyExpense(): Flow<Double?> = transactionDao.getMonthlyExpense()
    fun getMonthlyNet(): Flow<Double?> = transactionDao.getMonthlyNetSummary()
    fun getDailyNet(): Flow<List<NetData>> = transactionDao.getDailyNet()
    fun getWeeklyNet(): Flow<List<NetData>> = transactionDao.getWeeklyNet()
    fun getMonthlyNetList(): Flow<List<NetData>> = transactionDao.getMonthlyNet()

    fun getCategorySummary(): Flow<List<CategorySummary>> = transactionDao.getCategorySummary()
    
    fun getTotalBalance(): Flow<Double?> = walletDao.getTotalBalance()
    
    fun getWalletBalance(walletId: Int): Flow<Double?> = transactionDao.getWalletBalance(walletId)

    suspend fun getTransactionsByDateRange(start: Long, end: Long): List<TransactionEntity> = 
        transactionDao.getTransactionsByDateRange(start, end)

    fun getDailyExpensesByRange(start: Long, end: Long): Flow<List<com.example.testing.data.local.DailyExpense>> = 
        transactionDao.getDailyExpensesByRange(start, end)

    suspend fun updateWalletBalance(walletId: Int, amount: Double) {
        walletDao.updateBalance(walletId, amount)
    }

    suspend fun getTransactionCountForWallet(walletId: Int): Int = 
        transactionDao.getTransactionCountForWallet(walletId)

    fun getCreditTransactions(): Flow<List<TransactionEntity>> = transactionDao.getCreditTransactions()
    
    fun getPersonCreditBalances(): Flow<List<PersonCreditEntity>> = personCreditDao.getAllPersonCredits()

    suspend fun updatePersonCredit(personId: Int, delta: Double) {
        val current = personCreditDao.getPersonCreditById(personId)
        if (current == null) {
            personCreditDao.insertOrUpdate(PersonCreditEntity(personId, delta))
        } else {
            personCreditDao.updateCreditBalance(personId, delta)
        }
    }

    suspend fun recalculatePersonCredit(personId: Int) {
        val total = transactionDao.getPersonCreditSum(personId)
        personCreditDao.insertOrUpdate(PersonCreditEntity(personId, total))
    }
}
