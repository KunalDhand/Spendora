package com.example.testing.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonAngelDao {
    @Query("SELECT * FROM person_angel_balances")
    fun getAllPersonAngelBalances(): Flow<List<PersonAngelEntity>>

    @Query("SELECT * FROM person_angel_balances")
    suspend fun getAllPersonAngelBalancesOnce(): List<PersonAngelEntity>

    @Query("DELETE FROM person_angel_balances")
    suspend fun deleteAllPersonAngelBalances()

    @Query("SELECT * FROM person_angel_balances WHERE personId = :personId")
    suspend fun getPersonAngelById(personId: Int): PersonAngelEntity?

    @Query("DELETE FROM person_angel_balances WHERE personId = :personId")
    suspend fun deletePersonAngelById(personId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(personAngel: PersonAngelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personAngels: List<PersonAngelEntity>)

    @Query("UPDATE person_angel_balances SET angelBalance = angelBalance + :amount WHERE personId = :personId")
    suspend fun updateAngelBalance(personId: Int, amount: Double)
}
