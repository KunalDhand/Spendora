package com.example.testing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.repository.CategoryRepository
import com.example.testing.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories: Flow<List<CategoryEntity>> = repository.getAllCategories()

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insert(CategoryEntity(name = name))
        }
    }
}
