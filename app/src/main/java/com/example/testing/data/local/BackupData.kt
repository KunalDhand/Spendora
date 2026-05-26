package com.example.testing.data.local

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("transactions") val transactions: List<TransactionEntity>,
    @SerializedName("wallets") val wallets: List<WalletEntity>,
    @SerializedName("categories") val categories: List<CategoryEntity>,
    @SerializedName("persons") val persons: List<PersonEntity>,
    @SerializedName("tags") val tags: List<TagEntity>,
    @SerializedName("tag_cross_refs") val tagCrossRefs: List<TransactionTagCrossRef>,
    @SerializedName("person_credits") val personCredits: List<PersonCreditEntity>,
    @SerializedName("export_timestamp") val exportTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("version") val version: Int = 1
)
