package com.example.testing.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionSplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: TransactionSplitEntity)

    @Query("SELECT * FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun getSplitsForTransaction(transactionId: Int): List<TransactionSplitEntity>

    @Query("DELETE FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun deleteSplitsForTransaction(transactionId: Int)

    @Query("SELECT SUM(CASE WHEN isLent = 1 THEN amount ELSE -amount END) FROM transaction_splits WHERE personId = :personId")
    suspend fun getPersonSplitSum(personId: Int): Double

    @Query("SELECT * FROM transaction_splits")
    suspend fun getAllSplitsOnce(): List<TransactionSplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(splits: List<TransactionSplitEntity>)

    @Query("DELETE FROM transaction_splits")
    suspend fun deleteAllSplits()
}
