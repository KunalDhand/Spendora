package com.example.testing.data.local

import androidx.room.Entity

@Entity(
    tableName = "transaction_tag_cross_ref",
    primaryKeys = ["transactionId", "tagId"]
)
data class TransactionTagCrossRef(
    val transactionId: Int,
    val tagId: Int
)
