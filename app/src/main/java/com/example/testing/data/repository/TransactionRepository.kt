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
import com.example.testing.data.local.BackupData

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val walletDao: WalletDao,
    private val tagDao: TagDao,
    private val personDao: PersonDao,
    private val personCreditDao: PersonCreditDao,
    private val transactionSplitDao: com.example.testing.data.local.TransactionSplitDao,
    private val personAngelDao: com.example.testing.data.local.PersonAngelDao
) {
    suspend fun insertSplit(split: com.example.testing.data.local.TransactionSplitEntity) {
        transactionSplitDao.insertSplit(split)
    }

    suspend fun getSplitsForTransaction(transactionId: Int): List<com.example.testing.data.local.TransactionSplitEntity> {
        return transactionSplitDao.getSplitsForTransaction(transactionId)
    }

    suspend fun deleteSplitsForTransaction(transactionId: Int) {
        transactionSplitDao.deleteSplitsForTransaction(transactionId)
    }
    suspend fun getBackupData(): BackupData {
        return BackupData(
            transactions = transactionDao.getAllTransactionsOnce(),
            wallets = walletDao.getAllWalletsOnce(),
            categories = categoryDao.getAllCategoriesOnce(),
            persons = personDao.getAllPersonsOnce(),
            tags = tagDao.getAllTagsOnce(),
            tagCrossRefs = tagDao.getAllCrossRefsOnce(),
            personCredits = personCreditDao.getAllPersonCreditsOnce(),
            personAngelBalances = personAngelDao.getAllPersonAngelBalancesOnce(),
            transactionSplits = transactionSplitDao.getAllSplitsOnce()
        )
    }

    suspend fun restoreBackup(backup: BackupData) {
        // Clear all existing data
        transactionDao.deleteAllTransactions()
        tagDao.deleteAllCrossRefs()
        tagDao.deleteAllTags()
        walletDao.deleteAllWallets()
        categoryDao.deleteAllCategories()
        personDao.deleteAllPersons()
        personCreditDao.deleteAllPersonCredits()
        personAngelDao.deleteAllPersonAngelBalances()
        transactionSplitDao.deleteAllSplits()

        // Insert backup data
        // Order matters for foreign keys: persons/wallets/categories first, then transactions, then cross-refs
        personDao.insertAll(backup.persons)
        walletDao.insertAll(backup.wallets)
        categoryDao.insertAll(backup.categories)
        transactionDao.insertAll(backup.transactions)
        tagDao.insertTags(backup.tags)
        tagDao.insertCrossRefs(backup.tagCrossRefs)
        personCreditDao.insertAll(backup.personCredits)
        personAngelDao.insertAll(backup.personAngelBalances)
        transactionSplitDao.insertAll(backup.transactionSplits)
    }

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

    suspend fun deleteTagsForTransaction(transactionId: Int) {
        tagDao.deleteCrossRefsByTransactionId(transactionId)
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

    suspend fun getWalletByName(name: String): WalletEntity? = walletDao.getWalletByName(name)

    fun getCreditTransactions(): Flow<List<TransactionEntity>> = transactionDao.getCreditTransactions()
    
    fun getPersonCreditBalances(): Flow<List<PersonCreditEntity>> = personCreditDao.getAllPersonCredits()

    fun getPersonAngelBalances(): Flow<List<com.example.testing.data.local.PersonAngelEntity>> = 
        personAngelDao.getAllPersonAngelBalances()

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
        if (kotlin.math.abs(total) < 0.01) {
            personCreditDao.deletePersonCreditById(personId)
        } else {
            personCreditDao.insertOrUpdate(PersonCreditEntity(personId, total))
        }
    }

    suspend fun recalculatePersonAngel(personId: Int) {
        val total = transactionSplitDao.getPersonSplitSum(personId)
        if (kotlin.math.abs(total) < 0.01) {
            personAngelDao.deletePersonAngelById(personId)
        } else {
            personAngelDao.insertOrUpdate(com.example.testing.data.local.PersonAngelEntity(personId, total))
        }
    }
}
