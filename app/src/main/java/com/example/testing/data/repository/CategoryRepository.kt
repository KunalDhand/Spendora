package com.example.testing.data.repository

import com.example.testing.data.local.CategoryDao
import com.example.testing.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insert(category: CategoryEntity) = categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
}
