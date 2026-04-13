package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("walletId"),
        Index("categoryId"),
        Index("personId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val walletId: Int, // FROM wallet
    val toWalletId: Int? = null, // TO wallet (only for transfer)
    val categoryId: Int?,
    val type: String,
    val note: String?,
    val timestamp: Long,
    val personId: Int? = null
)
