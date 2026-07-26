package com.dersium.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.app.navigation.Screen
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MainUiState(val startDestination: Screen? = null, val accentHex: String = "#6366F1")

@HiltViewModel
class MainViewModel @Inject constructor(repo: UserPreferencesRepository) : ViewModel() {
    var isLoading by mutableStateOf(true)
        private set
    val uiState: StateFlow<MainUiState> = repo.userPreferences
        .onEach { isLoading = false }
        .map { prefs -> MainUiState(if (prefs.isPinEnabled) Screen.Auth else Screen.Home, prefs.themeAccentColor.hex) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())
}
