package com.example.testing.utils

import android.util.Log
import com.example.testing.data.local.AppDatabase
import com.example.testing.data.local.CategoryEntity
import com.example.testing.data.local.WalletEntity
import kotlinx.coroutines.flow.first

object PrepopulateData {

    suspend fun insertDefaults(db: AppDatabase) {
        val walletDao = db.walletDao()
        val categoryDao = db.categoryDao()

        // Clean up duplicates for Wallets
        walletDao.removeDuplicateWallets()

        // Ensure default Wallet exists
        if (walletDao.getWalletCount() == 0) {
            Log.d("DB_DEBUG", "No wallets found. Prepopulating default wallet...")
            walletDao.insert(WalletEntity(name = "Cash", type = "CASH", balance = 0.0, isDefault = true))
        }

        // 1. Rename old categories if they exist
        val currentCategories = categoryDao.getAllCategoriesOnce()
        
        currentCategories.find { it.name.equals("Food", ignoreCase = true) }?.let {
            Log.d("DB_DEBUG", "Renaming Food to Dining Out")
            categoryDao.update(it.copy(name = "Dining Out"))
        }
        
        currentCategories.find { it.name.equals("Transport", ignoreCase = true) }?.let {
            Log.d("DB_DEBUG", "Renaming Transport to Travel")
            categoryDao.update(it.copy(name = "Travel"))
        }

        // 2. Refresh list and remove duplicates that might have been created
        val afterRenameCategories = categoryDao.getAllCategoriesOnce()
        val seenNames = mutableSetOf<String>()
        afterRenameCategories.forEach { category ->
            val lowerName = category.name.lowercase().trim()
            if (seenNames.contains(lowerName)) {
                Log.d("DB_DEBUG", "Deleting duplicate category: ${category.name}")
                categoryDao.delete(category)
            } else {
                seenNames.add(lowerName)
            }
        }

        // 3. Add missing default categories
        val finalCategories = categoryDao.getAllCategoriesOnce().map { it.name.lowercase().trim() }
        val defaultCategories = listOf("Dining Out", "Travel", "Bills", "Shopping", "Rent", "Salary")
        
        defaultCategories.forEach { categoryName ->
            if (!finalCategories.contains(categoryName.lowercase())) {
                Log.d("DB_DEBUG", "Inserting default category: $categoryName")
                categoryDao.insert(CategoryEntity(name = categoryName))
            }
        }

        Log.d("DB_DEBUG", "Prepopulation check complete.")
    }
}
