package com.example.testing.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(wallet: WalletEntity): Long

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int

    @Query("DELETE FROM wallets WHERE id NOT IN (SELECT MIN(id) FROM wallets GROUP BY name)")
    suspend fun removeDuplicateWallets()

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets")
    suspend fun getAllWalletsOnce(): List<WalletEntity>

    @Query("UPDATE wallets SET balance = balance + :amount WHERE id = :walletId")
    suspend fun updateBalance(walletId: Int, amount: Double)

    @Query("SELECT * FROM wallets WHERE name = :name LIMIT 1")
    suspend fun getWalletByName(name: String): WalletEntity?
}
