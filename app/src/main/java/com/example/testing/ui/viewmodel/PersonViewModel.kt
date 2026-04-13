package com.example.testing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.local.PersonDao
import com.example.testing.data.local.PersonEntity
import kotlinx.coroutines.launch

class PersonViewModel(
    private val dao: PersonDao
) : ViewModel() {

    val persons = dao.getAllPersons()

    fun addPerson(name: String) {
        viewModelScope.launch {
            dao.insert(PersonEntity(name = name))
        }
    }
}
