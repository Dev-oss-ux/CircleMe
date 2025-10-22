package com.barry.circleme.ui.theme

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    init {
        _isDarkTheme.value = sharedPreferences.getBoolean("is_dark_theme", false)
    }

    fun toggleTheme() {
        viewModelScope.launch {
            _isDarkTheme.value = !_isDarkTheme.value
            sharedPreferences.edit().putBoolean("is_dark_theme", _isDarkTheme.value).apply()
        }
    }
}
