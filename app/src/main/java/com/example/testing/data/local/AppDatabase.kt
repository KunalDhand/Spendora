package com.example.testing.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        PersonEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class
    ],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun personDao(): PersonDao
    abstract fun tagDao(): TagDao
}
