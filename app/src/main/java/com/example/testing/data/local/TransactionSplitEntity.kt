package com.example.testing.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("personId")]
)
data class TransactionSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transactionId: Int,
    val personId: Int,
    val amount: Double,
    val isLent: Boolean // true if user lent money (user paid), false if user borrowed (someone else paid)
)
