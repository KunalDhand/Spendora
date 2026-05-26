package com.example.testing.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsOnce(): List<TagEntity>

    @Query("SELECT * FROM transaction_tag_cross_ref")
    suspend fun getAllCrossRefsOnce(): List<TransactionTagCrossRef>

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("DELETE FROM transaction_tag_cross_ref")
    suspend fun deleteAllCrossRefs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: TransactionTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<TransactionTagCrossRef>)

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId")
    suspend fun deleteCrossRefsByTransactionId(transactionId: Int)

    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN transaction_tag_cross_ref ON tags.id = transaction_tag_cross_ref.tagId 
        WHERE transactionId = :transactionId
    """)
    fun getTagsForTransaction(transactionId: Int): Flow<List<TagEntity>>

    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN transaction_tag_cross_ref ON tags.id = transaction_tag_cross_ref.tagId 
        WHERE transactionId = :transactionId
    """)
    suspend fun getTagsForTransactionOnce(transactionId: Int): List<TagEntity>
}
