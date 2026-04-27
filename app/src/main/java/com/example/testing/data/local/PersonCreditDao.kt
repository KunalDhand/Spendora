package com.example.testing.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonCreditDao {
    @Query("SELECT * FROM person_credits")
    fun getAllPersonCredits(): Flow<List<PersonCreditEntity>>

    @Query("SELECT * FROM person_credits WHERE personId = :personId")
    suspend fun getPersonCreditById(personId: Int): PersonCreditEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(personCredit: PersonCreditEntity)

    @Query("UPDATE person_credits SET creditBalance = creditBalance + :amount WHERE personId = :personId")
    suspend fun updateCreditBalance(personId: Int, amount: Double)
}
