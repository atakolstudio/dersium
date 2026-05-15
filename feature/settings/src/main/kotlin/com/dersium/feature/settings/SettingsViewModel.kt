package com.dersium.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Season
import com.dersium.core.domain.model.ThemeAccentColor
import com.dersium.core.domain.repository.FinancialRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val themeAccentColor: ThemeAccentColor = ThemeAccentColor.INDIGO,
    val currency: String = "₺",
    val isPremium: Boolean = false,
    val activeSeasonId: Long = 1L,
    val activeSeasonName: String = "",
    val seasons: List<Season> = emptyList(),
    // PIN setup
    val showPinSetup: Boolean = false,
    val newPin: String = "",
    val confirmPin: String = "",
    val pinError: String? = null,
    val pinStep: PinSetupStep = PinSetupStep.ENTER_NEW,
)

enum class PinSetupStep { ENTER_NEW, CONFIRM }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val financialRepository: FinancialRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        financialRepository.getAllSeasons(),
        financialRepository.getActiveSeason(),
    ) { prefs, seasons, activeSeason ->
        SettingsUiState(
            isPinEnabled = prefs.isPinEnabled,
            isBiometricEnabled = prefs.isBiometricEnabled,
            themeAccentColor = prefs.themeAccentColor,
            currency = prefs.currency,
            isPremium = prefs.isPremium,
            activeSeasonId = prefs.activeSeasonId,
            activeSeasonName = activeSeason?.displayName ?: "",
            seasons = seasons,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _state = MutableStateFlow(SettingsUiState())
    // PIN setup state (ephemeral)
    private val _pinState = MutableStateFlow(PinSetupData())
    val pinState: StateFlow<PinSetupData> = _pinState.asStateFlow()

    fun showPinSetup() { _pinState.update { PinSetupData(isVisible = true) } }
    fun hidePinSetup() { _pinState.update { PinSetupData() } }

    fun onPinDigit(digit: Int) {
        val p = _pinState.value
        val current = if (p.step == PinSetupStep.ENTER_NEW) p.newPin else p.confirmPin
        if (current.length >= 4) return
        val next = current + digit.toString()
        if (p.step == PinSetupStep.ENTER_NEW) {
            _pinState.update { it.copy(newPin = next) }
            if (next.length == 4) _pinState.update { it.copy(step = PinSetupStep.CONFIRM, confirmPin = "") }
        } else {
            _pinState.update { it.copy(confirmPin = next) }
            if (next.length == 4) verifyAndSavePin(p.newPin, next)
        }
    }

    fun onPinBackspace() {
        val p = _pinState.value
        if (p.step == PinSetupStep.ENTER_NEW) _pinState.update { it.copy(newPin = p.newPin.dropLast(1)) }
        else _pinState.update { it.copy(confirmPin = p.confirmPin.dropLast(1)) }
    }

    private fun verifyAndSavePin(newPin: String, confirmPin: String) {
        if (newPin == confirmPin) {
            viewModelScope.launch {
                userPreferencesRepository.setPinCode(newPin)
                _pinState.update { PinSetupData() }
            }
        } else {
            _pinState.update { it.copy(step = PinSetupStep.ENTER_NEW, newPin = "", confirmPin = "", error = "PIN'ler eşleşmiyor, tekrar deneyin") }
        }
    }

    fun disablePin() { viewModelScope.launch { userPreferencesRepository.clearPinCode() } }
    fun setBiometric(enabled: Boolean) { viewModelScope.launch { userPreferencesRepository.setBiometricEnabled(enabled) } }
    fun setThemeAccent(color: ThemeAccentColor) { viewModelScope.launch { userPreferencesRepository.setThemeAccentColor(color) } }

    fun createNewSeason(name: String, startYear: Int, endYear: Int) {
        viewModelScope.launch {
            val seasonId = financialRepository.insertSeason(
                Season(
                    name = name,
                    startYear = startYear,
                    endYear = endYear,
                    isActive = false,
                )
            )
            userPreferencesRepository.setActiveSeasonId(seasonId)
        }
    }

    fun switchSeason(seasonId: Long) {
        viewModelScope.launch { userPreferencesRepository.setActiveSeasonId(seasonId) }
    }
}

data class PinSetupData(
    val isVisible: Boolean = false,
    val step: PinSetupStep = PinSetupStep.ENTER_NEW,
    val newPin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
)
