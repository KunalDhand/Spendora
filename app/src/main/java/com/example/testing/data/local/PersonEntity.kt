package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
