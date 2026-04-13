package com.example.testing.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.testing.data.repository.CategoryRepository
import com.example.testing.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories: Flow<List<CategoryEntity>> = repository.getAllCategories()
}
