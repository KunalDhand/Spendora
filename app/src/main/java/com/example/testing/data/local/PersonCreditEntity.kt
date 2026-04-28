package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_credits")
data class PersonCreditEntity(
    @PrimaryKey val personId: Int,
    val creditBalance: Double
)
