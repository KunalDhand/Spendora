package com.example.testing.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing.data.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ThemeLoadState {
    object Loading : ThemeLoadState()
    data class Ready(val isDark: Boolean?) : ThemeLoadState()
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _themeState = MutableStateFlow<ThemeLoadState>(ThemeLoadState.Loading)
    val themeState: StateFlow<ThemeLoadState> = _themeState.asStateFlow()

    init {
        viewModelScope.launch {
            ThemePreference.getTheme(application).collect { pref ->
                _themeState.value = ThemeLoadState.Ready(pref)
            }
        }
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        viewModelScope.launch {
            ThemePreference.saveTheme(getApplication(), !isCurrentlyDark)
        }
    }
}
