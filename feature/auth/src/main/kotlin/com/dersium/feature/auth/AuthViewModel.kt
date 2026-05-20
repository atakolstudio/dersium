package com.dersium.feature.auth

import com.dersium.feature.auth.PinHasher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val pinCode: String = "",
    val enteredDigits: String = "",
    val error: String? = null,
    val isBiometricEnabled: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authSuccess = MutableSharedFlow<Unit>()
    val authSuccess: SharedFlow<Unit> = _authSuccess.asSharedFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        pinCode = prefs.pinCode,
                        isBiometricEnabled = prefs.isBiometricEnabled,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onDigitEntered(digit: Int) {
        val current = _uiState.value.enteredDigits
        if (current.length >= 4) return
        val newPin = current + digit.toString()
        _uiState.update { it.copy(enteredDigits = newPin, error = null) }
        if (newPin.length == 4) verifyPin(newPin)
    }

    fun onBackspace() {
        val current = _uiState.value.enteredDigits
        if (current.isEmpty()) return
        _uiState.update { it.copy(enteredDigits = current.dropLast(1), error = null) }
    }

    private fun verifyPin(entered: String) {
        viewModelScope.launch {
            if (entered == _uiState.value.pinCode) {
                _authSuccess.emit(Unit)
            } else {
                _uiState.update { it.copy(enteredDigits = "", error = "Yanlış PIN, tekrar deneyin") }
            }
        }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch { _authSuccess.emit(Unit) }
    }
}
