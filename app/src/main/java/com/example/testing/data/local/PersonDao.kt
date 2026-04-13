package com.example.testing.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Insert
    suspend fun insert(person: PersonEntity)

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons")
    suspend fun getAllPersonsOnce(): List<PersonEntity>
}
