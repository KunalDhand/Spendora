package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallets",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String = "OTHER",
    val balance: Double = 0.0,
    val isDefault: Boolean = false
)
