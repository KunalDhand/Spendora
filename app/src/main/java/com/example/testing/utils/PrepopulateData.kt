package com.example.testing.utils

import android.util.Log
import com.example.testing.data.local.AppDatabase
import com.example.testing.data.local.CategoryEntity
import com.example.testing.data.local.WalletEntity

object PrepopulateData {

    suspend fun insertDefaults(db: AppDatabase) {

        val walletDao = db.walletDao()
        val categoryDao = db.categoryDao()

        // Clean up duplicates first (in case any exist from previous bug)
        walletDao.removeDuplicateWallets()

        // Only insert "Cash" as default if no wallets exist
        if (walletDao.getWalletCount() == 0) {
            Log.d("DB_DEBUG", "No data found. Prepopulating default wallet...")
            // Default Wallet
            walletDao.insert(WalletEntity(name = "Cash", type = "CASH", balance = 0.0, isDefault = true))

            // Categories
            categoryDao.insert(CategoryEntity(name = "Food"))
            categoryDao.insert(CategoryEntity(name = "Transport"))
            categoryDao.insert(CategoryEntity(name = "Bills"))
            categoryDao.insert(CategoryEntity(name = "Shopping"))
            Log.d("DB_DEBUG", "Prepopulation complete.")
        } else {
            Log.d("DB_DEBUG", "Data already exists. Skipping prepopulation.")
        }
    }
}
