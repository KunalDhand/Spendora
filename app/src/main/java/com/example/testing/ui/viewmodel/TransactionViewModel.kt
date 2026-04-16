package com.example.testing.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.repository.TransactionRepository
import com.example.testing.data.local.TransactionEntity
import com.example.testing.data.local.CategorySummary
import com.example.testing.data.local.DailyExpense
import com.example.testing.data.local.NetData
import com.example.testing.utils.DateFilter
import com.example.testing.utils.DateUtils
import com.example.testing.utils.PdfExporter
import com.example.testing.utils.ExportTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class CategoryUI(
    val name: String,
    val total: Double
)

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    suspend fun exportToPdf(
        context: android.content.Context,
        filter: DateFilter,
        customStart: Long? = null,
        customEnd: Long? = null
    ) {
        val (start, end) = DateUtils.getDateRange(filter, customStart, customEnd)
        val transactions = repository.getTransactionsByDateRange(start, end)
        
        val categories = repository.getAllCategoriesOnce()
        val wallets = repository.getAllWalletsOnce()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        val dateRangeText = if (filter == DateFilter.CUSTOM && customStart != null && customEnd != null) {
            "${dateFormat.format(Date(customStart))} - ${dateFormat.format(Date(customEnd))}"
        } else {
            filter.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
        }

        val exportList = transactions.map { tx ->
            val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Unknown"
            val walletName = wallets.find { it.id == tx.walletId }?.name ?: "Unknown"
            val dateStr = dateFormat.format(Date(tx.timestamp))

            ExportTransaction(
                type = tx.type,
                amount = tx.amount,
                categoryName = categoryName,
                walletName = walletName,
                date = dateStr
            )
        }

        val fileName = "Expense_Report_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        PdfExporter.export(file, exportList, dateRangeText)
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(android.content.Intent.createChooser(intent, "Share Expense Report"))
    }

    val transactions: Flow<List<TransactionEntity>> = repository.getAllTransactions()
    
    // Today's Flows
    val todayIncome: Flow<Double?> = repository.getTodayIncome()
    val todayExpense: Flow<Double?> = repository.getTodayExpense()
    val todayNet: Flow<Double?> = repository.getTodayNet()

    // Monthly Flows
    val monthlyIncome: Flow<Double?> = repository.getMonthlyIncome()
    val monthlyExpense: Flow<Double?> = repository.getMonthlyExpense()
    val monthlyNet: Flow<Double?> = repository.getMonthlyNet()

    val totalBalance: Flow<Double?> = repository.getTotalBalance()
    
    fun getDailyExpensesByRange(start: Long, end: Long): Flow<List<DailyExpense>> = 
        repository.getDailyExpensesByRange(start, end)

    fun getDailyNet(): Flow<List<NetData>> = repository.getDailyNet()
    fun getWeeklyNet(): Flow<List<NetData>> = repository.getWeeklyNet()
    fun getMonthlyNetList(): Flow<List<NetData>> = repository.getMonthlyNetList()

    private val _categorySummary: Flow<List<CategorySummary>> = repository.getCategorySummary()

    fun getCategoryUIList(categories: List<com.example.testing.data.local.CategoryEntity>): Flow<List<CategoryUI>> {
        return _categorySummary.combine(kotlinx.coroutines.flow.flowOf(categories)) { summary, categoryList ->
            summary.map { item ->
                val categoryName = categoryList.find { it.id == item.categoryId }?.name ?: "Unknown"
                CategoryUI(categoryName, item.total)
            }
        }
    }

    fun getTransactionsUI(): Flow<List<TransactionUI>> {
        return repository.getAllTransactions().map { transactions ->
            val categories = repository.getAllCategoriesOnce()
            val wallets = repository.getAllWalletsOnce()
            val persons = repository.getAllPersonsOnce()
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            val uiList = mutableListOf<TransactionUI>()
            for (tx in transactions) {
                val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Unknown"
                val walletName = wallets.find { it.id == tx.walletId }?.name ?: "Unknown"
                val toWalletName = tx.toWalletId?.let { id -> wallets.find { it.id == id }?.name }
                val personName = persons.find { it.id == tx.personId }?.name
                
                // Fetch tags for this transaction
                val tags = repository.getTagsForTransactionOnce(tx.id)
                val tagNames = tags.map { it.name }
                val tagIds = tags.map { it.id }
                
                uiList.add(
                    TransactionUI(
                        id = tx.id,
                        amount = tx.amount,
                        type = tx.type,
                        date = dateFormat.format(Date(tx.timestamp)),
                        time = timeFormat.format(Date(tx.timestamp)),
                        category = categoryName,
                        wallet = walletName,
                        person = personName,
                        personId = tx.personId,
                        categoryId = tx.categoryId,
                        walletId = tx.walletId,
                        toWallet = toWalletName,
                        toWalletId = tx.toWalletId,
                        tags = tagNames,
                        tagIds = tagIds,
                        note = tx.note
                    )
                )
            }
            uiList
        }
    }

    fun getWalletBalance(walletId: Int): Flow<Double?> = repository.getWalletBalance(walletId)

    fun deleteTransaction(txId: Int) {
        viewModelScope.launch {
            val tx = repository.getTransactionById(txId) ?: return@launch
            
            // Revert wallet balance
            when (tx.type) {
                "INCOME" -> repository.updateWalletBalance(tx.walletId, -tx.amount)
                "EXPENSE" -> repository.updateWalletBalance(tx.walletId, tx.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(tx.walletId, tx.amount)
                    tx.toWalletId?.let { repository.updateWalletBalance(it, -tx.amount) }
                }
            }
            repository.delete(tx)
        }
    }

    fun restoreTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.insert(tx)
            
            // Re-apply wallet balance
            when (tx.type) {
                "INCOME" -> repository.updateWalletBalance(tx.walletId, tx.amount)
                "EXPENSE" -> repository.updateWalletBalance(tx.walletId, -tx.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(tx.walletId, -tx.amount)
                    tx.toWalletId?.let { repository.updateWalletBalance(it, tx.amount) }
                }
            }
        }
    }

    suspend fun getTransactionEntityById(id: Int): TransactionEntity? = repository.getTransactionById(id)

    fun addTransaction(transaction: TransactionEntity, tagIds: List<Int> = emptyList()) {
        viewModelScope.launch {
            Log.d("DB_DEBUG", "Inserting transaction: $transaction")
            val txId = repository.insert(transaction)
            Log.d("TAG_DEBUG", "Inserted Transaction ID: $txId")
            
            // Update wallet balance
            when (transaction.type) {
                "EXPENSE" -> repository.updateWalletBalance(transaction.walletId, -transaction.amount)
                "INCOME" -> repository.updateWalletBalance(transaction.walletId, transaction.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(transaction.walletId, -transaction.amount)
                    transaction.toWalletId?.let { 
                        repository.updateWalletBalance(it, transaction.amount) 
                    }
                }
            }

            tagIds.forEach { tagId ->
                Log.d("TAG_DEBUG", "Saving tagId=$tagId for txId=$txId")
                repository.addTagToTransaction(txId, tagId)
            }
            Log.d("DB_DEBUG", "Transaction and tags inserted successfully")
        }
    }
}
