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
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import android.widget.Toast
import com.example.testing.data.local.BackupData
import com.google.gson.GsonBuilder
import java.io.OutputStreamWriter
import android.content.Context
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

data class CategoryUI(
    val name: String,
    val total: Double
)

class TransactionViewModel(
    val repository: TransactionRepository
) : ViewModel() {

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val gson = com.google.gson.Gson()
                        val backupData = gson.fromJson(reader, BackupData::class.java)
                        
                        if (backupData != null) {
                            repository.restoreBackup(backupData)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Failed to parse backup file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error restoring backup: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backupData = repository.getBackupData()
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(backupData)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup saved successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun exportToPdf(
        context: Context,
        filter: DateFilter,
        customStart: Long? = null,
        customEnd: Long? = null
    ) {
        val (start, end) = DateUtils.getDateRange(filter, customStart, customEnd)
        val transactions = repository.getTransactionsByDateRange(start, end)
        
        val categories = repository.getAllCategoriesOnce()
        val wallets = repository.getAllWalletsOnce()
        val persons = repository.getAllPersonsOnce()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        
        val dateRangeText = if (filter == DateFilter.CUSTOM && customStart != null && customEnd != null) {
            "${dateFormat.format(Date(customStart))} - ${dateFormat.format(Date(customEnd))}"
        } else {
            filter.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
        }

        val exportList = transactions.map { tx ->
            val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Unknown"
            val fromWallet = wallets.find { it.id == tx.walletId }?.name ?: "Unknown"
            val toWallet = if (tx.type == "TRANSFER") {
                wallets.find { it.id == tx.toWalletId }?.name ?: "Unknown"
            } else null
            val walletDisplay = if (toWallet != null) "$fromWallet -> $toWallet" else fromWallet
            
            val personName = persons.find { it.id == tx.personId }?.name
            val tags = repository.getTagsForTransactionOnce(tx.id).joinToString(", ") { it.name }

            ExportTransaction(
                type = tx.type,
                amount = tx.amount,
                categoryName = categoryName,
                walletName = walletDisplay,
                date = dateFormat.format(Date(tx.timestamp)),
                time = timeFormat.format(Date(tx.timestamp)),
                personName = personName,
                note = tx.note,
                tags = tags,
                isCredit = tx.isCredit
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

    fun getCreditTransactionsUI(): Flow<List<TransactionUI>> {
        return repository.getCreditTransactions().map { transactions ->
            val categories = repository.getAllCategoriesOnce()
            val wallets = repository.getAllWalletsOnce()
            val persons = repository.getAllPersonsOnce()
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            transactions.map { tx ->
                val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Unknown"
                val walletName = wallets.find { it.id == tx.walletId }?.name ?: "Unknown"
                val toWalletName = tx.toWalletId?.let { id -> wallets.find { it.id == id }?.name }
                val personName = persons.find { it.id == tx.personId }?.name
                
                val tags = repository.getTagsForTransactionOnce(tx.id)
                
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
                    tags = tags.map { it.name },
                    tagIds = tags.map { it.id },
                    note = tx.note,
                    isCredit = tx.isCredit,
                    hasSplits = repository.getSplitsForTransaction(tx.id).isNotEmpty()
                )
            }
        }
    }

    fun getPersonCreditBalances(): Flow<List<com.example.testing.data.local.PersonCreditEntity>> = 
        repository.getPersonCreditBalances()

    fun getPersonAngelBalances(): Flow<List<com.example.testing.data.local.PersonAngelEntity>> = 
        repository.getPersonAngelBalances()

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
                        note = tx.note,
                        isCredit = tx.isCredit,
                        hasSplits = repository.getSplitsForTransaction(tx.id).isNotEmpty()
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
            val splits = repository.getSplitsForTransaction(txId)
            
            // Revert impacts
            when (tx.type) {
                "INCOME" -> repository.updateWalletBalance(tx.walletId, -tx.amount)
                "EXPENSE" -> repository.updateWalletBalance(tx.walletId, tx.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(tx.walletId, tx.amount)
                    tx.toWalletId?.let { repository.updateWalletBalance(it, -tx.amount) }
                }
            }
            updateAngelBalanceForTransaction(tx, splits, isReverting = true)

            repository.delete(tx)

            // Recalculate
            val allPeople = mutableSetOf<Int>()
            tx.personId?.let { allPeople.add(it) }
            splits.forEach { allPeople.add(it.personId) }

            allPeople.forEach { personId ->
                repository.recalculatePersonCredit(personId)
                repository.recalculatePersonAngel(personId)
            }
        }
    }

    private suspend fun updateAngelBalanceForTransaction(
        tx: TransactionEntity,
        splits: List<com.example.testing.data.local.TransactionSplitEntity>,
        isReverting: Boolean
    ) {
        val angelWallet = repository.getWalletByName("ANGEL") ?: return
        val multiplier = if (isReverting) -1.0 else 1.0
        
        var adjustment = 0.0
        
        // Splits: what OTHERS owe me / I spent on others
        if (splits.isNotEmpty()) {
            val splitSum = splits.sumOf { it.amount }
            adjustment += splitSum
        }
        
        // Note: Direct Credits (tx.isCredit) are NO LONGER added to ANGEL wallet balance
        // because we are keeping Angel (generosity) and Credit (loans) separate.
        
        if (adjustment != 0.0) {
            repository.updateWalletBalance(angelWallet.id, adjustment * multiplier)
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

            // Re-apply Credit Balance if applicable
            if (tx.isCredit && tx.personId != null) {
                val delta = if (tx.type == "INCOME") tx.amount else -tx.amount
                repository.updatePersonCredit(tx.personId, delta)
            }
            
            // Note: Full split restoration for ANGEL balance in 'restore' is omitted 
            // as restore usually handles single entities from a recent undo.
        }
    }

    suspend fun getTransactionEntityById(id: Int): TransactionEntity? = repository.getTransactionById(id)

    fun addTransaction(transaction: TransactionEntity, tagIds: List<Int> = emptyList(), splits: List<com.example.testing.data.local.TransactionSplitEntity> = emptyList()) {
        viewModelScope.launch {
            Log.d("DB_DEBUG", "Inserting transaction: $transaction")
            
            // Automatic ANGEL wallet selection for "Someone Else Paid"
            // Note: isCredit is NO LONGER forced to true for ANGEL because splits go to PersonAngelEntity
            val angelWallet = repository.getWalletByName("ANGEL")
            val isSomeoneElsePaid = splits.isNotEmpty() && !splits.first().isLent
            val txToSave = if (isSomeoneElsePaid && angelWallet != null && transaction.type == "EXPENSE") {
                transaction.copy(walletId = angelWallet.id)
            } else {
                transaction
            }

            val txId = repository.insert(txToSave)
            
            // Update wallet balance
            when (txToSave.type) {
                "EXPENSE" -> repository.updateWalletBalance(txToSave.walletId, -txToSave.amount)
                "INCOME" -> repository.updateWalletBalance(txToSave.walletId, txToSave.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(txToSave.walletId, -txToSave.amount)
                    txToSave.toWalletId?.let { repository.updateWalletBalance(it, txToSave.amount) }
                }
            }

            // Update ANGEL wallet social adjustments
            updateAngelBalanceForTransaction(txToSave, splits, isReverting = false)

            // Update Credit Balance (Only for direct loans/borrowing)
            txToSave.personId?.let { repository.recalculatePersonCredit(it) }

            // Handle Splits -> Now updates PersonAngel instead of PersonCredit
            splits.forEach { split ->
                val splitWithTxId = split.copy(transactionId = txId.toInt())
                repository.insertSplit(splitWithTxId)
                repository.recalculatePersonAngel(split.personId)
            }

            tagIds.forEach { tagId ->
                repository.addTagToTransaction(txId, tagId)
            }
        }
    }

    fun updateTransaction(updatedTx: TransactionEntity, newTagIds: List<Int>, newSplits: List<com.example.testing.data.local.TransactionSplitEntity> = emptyList()) {
        viewModelScope.launch {
            val oldTx = repository.getTransactionById(updatedTx.id) ?: return@launch
            val oldSplits = repository.getSplitsForTransaction(updatedTx.id)

            val angelWallet = repository.getWalletByName("ANGEL")
            val isSomeoneElsePaid = newSplits.isNotEmpty() && !newSplits.first().isLent
            val finalUpdatedTx = if (isSomeoneElsePaid && angelWallet != null && updatedTx.type == "EXPENSE") {
                updatedTx.copy(walletId = angelWallet.id)
            } else {
                updatedTx
            }

            // Revert impacts
            when (oldTx.type) {
                "INCOME" -> repository.updateWalletBalance(oldTx.walletId, -oldTx.amount)
                "EXPENSE" -> repository.updateWalletBalance(oldTx.walletId, oldTx.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(oldTx.walletId, oldTx.amount)
                    oldTx.toWalletId?.let { repository.updateWalletBalance(it, -oldTx.amount) }
                }
            }
            updateAngelBalanceForTransaction(oldTx, oldSplits, isReverting = true)

            // Update
            repository.update(finalUpdatedTx)
            repository.deleteTagsForTransaction(finalUpdatedTx.id)
            newTagIds.forEach { repository.addTagToTransaction(finalUpdatedTx.id.toLong(), it) }
            repository.deleteSplitsForTransaction(finalUpdatedTx.id)
            newSplits.forEach { repository.insertSplit(it.copy(transactionId = finalUpdatedTx.id)) }

            // Apply new impacts
            when (finalUpdatedTx.type) {
                "INCOME" -> repository.updateWalletBalance(finalUpdatedTx.walletId, finalUpdatedTx.amount)
                "EXPENSE" -> repository.updateWalletBalance(finalUpdatedTx.walletId, -finalUpdatedTx.amount)
                "TRANSFER" -> {
                    repository.updateWalletBalance(finalUpdatedTx.walletId, -finalUpdatedTx.amount)
                    finalUpdatedTx.toWalletId?.let { repository.updateWalletBalance(it, finalUpdatedTx.amount) }
                }
            }
            updateAngelBalanceForTransaction(finalUpdatedTx, newSplits, isReverting = false)

            // Recalculate PersonAngel and PersonCredit for everyone
            val allPersonIds = mutableSetOf<Int>()
            oldTx.personId?.let { allPersonIds.add(it) }
            finalUpdatedTx.personId?.let { allPersonIds.add(it) }
            oldSplits.forEach { allPersonIds.add(it.personId) }
            newSplits.forEach { allPersonIds.add(it.personId) }

            allPersonIds.forEach { personId ->
                repository.recalculatePersonCredit(personId)
                repository.recalculatePersonAngel(personId)
            }
        }
    }
}
