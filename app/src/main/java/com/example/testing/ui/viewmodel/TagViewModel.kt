package com.example.testing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.local.TagEntity
import com.example.testing.data.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TagViewModel(
    private val repository: TagRepository
) : ViewModel() {

    val allTags: Flow<List<TagEntity>> = repository.allTags

    fun addTag(name: String) {
        viewModelScope.launch {
            repository.insertTag(TagEntity(name = name))
        }
    }
}
