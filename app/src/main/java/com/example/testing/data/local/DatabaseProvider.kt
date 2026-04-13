package com.example.testing.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {

    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "expense_db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Insert default values on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            
                            // Default Wallet
                            database.walletDao().insert(WalletEntity(name = "Cash", type = "CASH", balance = 0.0, isDefault = true))
                            
                            // Default Categories
                            database.categoryDao().insert(CategoryEntity(name = "Food"))
                            database.categoryDao().insert(CategoryEntity(name = "Transport"))
                        }
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
