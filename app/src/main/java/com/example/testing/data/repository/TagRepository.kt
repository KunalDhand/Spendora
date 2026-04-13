package com.example.testing.data.repository

import com.example.testing.data.local.TagDao
import com.example.testing.data.local.TagEntity
import com.example.testing.data.local.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun insertTag(tag: TagEntity): Long {
        return tagDao.insertTag(tag)
    }

    suspend fun insertCrossRef(crossRef: TransactionTagCrossRef) {
        tagDao.insertCrossRef(crossRef)
    }

    fun getTagsForTransaction(transactionId: Int): Flow<List<TagEntity>> {
        return tagDao.getTagsForTransaction(transactionId)
    }
}
