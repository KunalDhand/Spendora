package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_angel_balances")
data class PersonAngelEntity(
    @PrimaryKey val personId: Int,
    val angelBalance: Double
)
